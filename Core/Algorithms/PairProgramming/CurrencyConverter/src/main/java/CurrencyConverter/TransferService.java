package CurrencyConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;


class TransferService {
    private final UserRepository userRepository;
    private final ExchangeRateProvider exchangeRateProvider;
    private final Duration cacheTTL;
    private final Map<CurrencyPair, CachedRate> ratesCache = new HashMap<>();
    private final Map<String, TransferResult> completedTransfers = new HashMap<>();
    private final Clock clock;

    public TransferService(
            UserRepository userRepository,
            ExchangeRateProvider exchangeRateProvider,
            Duration cacheTTL,
            Clock clock) {
        // Validate configuration before constructing the service.
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must not be null");
        }

        if (exchangeRateProvider == null) {
            throw new IllegalArgumentException("exchangeRateProvider must not be null");
        }

        if (cacheTTL == null) {
            throw new IllegalArgumentException("cacheTTL must not be null");
        }

        if (cacheTTL.isZero() || cacheTTL.isNegative()) {
            throw new IllegalArgumentException(
                    "cacheTTL must be greater than zero"
            );
        }

        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }

        this.userRepository = userRepository;
        this.exchangeRateProvider = exchangeRateProvider;
        this.cacheTTL = cacheTTL;
        this.clock = clock;
    }

    public TransferResult send(
            String transferId,
            String senderId,
            String recipientId,
            BigDecimal amount,
            String sourceCurrency,
            String targetCurrency) {

        if (transferId == null || transferId.isBlank()) {
            throw new IllegalArgumentException("invalid transfer ID");
        }

        // idempotency simulation
        TransferResult transferResult = completedTransfers.get(transferId);
        if (transferResult != null) {
            return transferResult;
        }

        validateTransferInputs(senderId, recipientId, amount, sourceCurrency, targetCurrency);

        User sender = requireUser(senderId, "sender");
        User recipient = requireUser(recipientId, "recipient");

        Account senderAccount = requireAccount(
                sender, sourceCurrency, "sender does not have the source currency account");
        Account recipientAccount = requireAccount(
                recipient, targetCurrency, "recipient does not have the target currency account");

        requireSufficientFunds(senderAccount, amount);

        /**
         *  Complete the transfer without calling the exchange rate provider
         *  when the source currency is the same as the target currency except otherwise
         */
        BigDecimal convertedAmount = amount;
        boolean isDifferentCurrency = !sourceCurrency.equals(targetCurrency);
        BigDecimal rate = BigDecimal.ONE;

        if (isDifferentCurrency) {
            rate = resolveRate(sourceCurrency, targetCurrency);
            // Perform the conversion using the full precision of the
            // exchange rate. Rounding is applied to the resulting money
            // amount, not to the exchange rate.
            convertedAmount = amount.multiply(rate);
        }

        convertedAmount = applyTargetCurrencyPrecision(convertedAmount, targetCurrency);

        // side effects only happen after prerequisites succeed
        senderAccount.debit(amount);
        recipientAccount.credit(convertedAmount);

        transferResult = new TransferResult(
                transferId,
                TransferStatus.SUCCESS,
                amount,
                sourceCurrency,
                convertedAmount,
                targetCurrency,
                rate
        );

        completedTransfers.put(transferId, transferResult);

        return transferResult;
    }

    // basic argument validation
    private void validateTransferInputs(
            String senderId,
            String recipientId,
            BigDecimal amount,
            String sourceCurrency,
            String targetCurrency) {

        if (senderId == null || senderId.isBlank()) {
            throw new IllegalArgumentException("invalid senderId");
        }

        if (recipientId == null || recipientId.isBlank()) {
            throw new IllegalArgumentException("invalid recipientId");
        }

        if (sourceCurrency == null || sourceCurrency.isBlank()) {
            throw new IllegalArgumentException("source currency is empty");
        }

        if (targetCurrency == null || targetCurrency.isBlank()) {
            throw new IllegalArgumentException("target currency is empty");
        }

        // Validate that the supplied currency codes are supported.
        // Fail early before repository lookups or external provider calls.
        try {
            Currency.getInstance(sourceCurrency);
            Currency.getInstance(targetCurrency);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unsupported currency", e);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }

    // entity existence validation
    private User requireUser(String userId, String role) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException(role + " does not exist");
        }
        return user;
    }

    // domain state validation
    private Account requireAccount(User user, String currency, String missingAccountMessage) {
        Account account = user.getAccount(currency);
        if (account == null) {
            throw new IllegalStateException(missingAccountMessage);
        }
        return account;
    }

    private void requireSufficientFunds(Account senderAccount, BigDecimal amount) {
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("insufficient funds");
        }
    }

    // Determine how many fractional digits the target currency supports.
    // For example:
    // EUR / GBP -> 2 decimal places
    // JPY       -> 0 decimal places
    //
    // Round the final amount that will actually be credited.
    // HALF_EVEN is the rounding policy chosen for this practice version.
    //
    // This happens after conversion so that the exchange rate retains
    // its full precision, and before account balances are modified.
    private BigDecimal applyTargetCurrencyPrecision(BigDecimal amount, String targetCurrency) {
        int scale = Currency.getInstance(targetCurrency).getDefaultFractionDigits();
        return amount.setScale(scale, RoundingMode.HALF_EVEN);
    }

    private BigDecimal resolveRate(String sourceCurrency, String targetCurrency) {
            CurrencyPair pair = new CurrencyPair(sourceCurrency, targetCurrency);
            CachedRate cachedRate = ratesCache.get(pair);
            Instant now = Instant.now(clock);

            // fresh cache hit
            if (cachedRate != null && now.isBefore(cachedRate.expiresAt())) {
                return cachedRate.rate();
            } else {
                try {
                    // cache miss or stale cache -> try provider
                    BigDecimal rate = exchangeRateProvider.getRate(sourceCurrency, targetCurrency);

                    // Never allow an unusable rate to enter the cache.
                    if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalStateException("invalid rate");
                    }

                    // rate becomes fresh at this point
                    Instant expiresAt = Instant.now(clock).plus(cacheTTL);

                    // set fresh cache
                    ratesCache.put(pair, new CachedRate(rate, expiresAt));

                    return rate;

                } catch (ExchangeRateException e) {
                    // exchange rate provider fails here
                    // If cachedRate exists here, it must be stale,
                    // because a fresh cache would have been handled above.
                    if (cachedRate != null) {
                        return cachedRate.rate();
                    } else {
                        // No fresh or stale rate is available, so the transfer
                        // cannot safely continue.
                        throw e;
                    }
                }
            }
    }

    public BigDecimal receive(String userId, String currency) {
        User user = userRepository.findById(userId);
        return user.getAccount(currency).getBalance();
    }
}
