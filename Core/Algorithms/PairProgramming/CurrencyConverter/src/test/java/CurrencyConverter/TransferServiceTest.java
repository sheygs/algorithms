package CurrencyConverter;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferServiceTest {

    @Test
    void shouldReuseCachedRateBeforeTtlExpires() {
        User sender = new User("sender-1");
        Account senderAccount = new Account("GBP", new BigDecimal("1000.00"));
        sender.addAccount(senderAccount);

        User recipient = new User("recipient-1");
        Account recipientAccount = new Account("EUR", new BigDecimal("0.00"));
        recipient.addAccount(recipientAccount);

        UserRepository userRepository = userId -> {
            if ("sender-1".equals(userId)) {
                return sender;
            }
            if ("recipient-1".equals(userId)) {
                return recipient;
            }
            return null;
        };

        AtomicInteger providerCallCount = new AtomicInteger();

        ExchangeRateProvider exchangeRateProvider =
                (fromCurrency, toCurrency) -> {
                    providerCallCount.incrementAndGet();
                    return new BigDecimal("1.20");
                };

        MutableClock clock = new MutableClock(
                Instant.parse("2026-08-12T10:00:00Z")
        );

        TransferService transferService =
                new TransferService(
                        userRepository,
                        exchangeRateProvider,
                        Duration.ofSeconds(30),
                        clock
                );

        TransferResult firstResult = transferService.send(
                "tx-1",
                "sender-1",
                "recipient-1",
                new BigDecimal("100.00"),
                "GBP",
                "EUR"
        );

        clock.advance(Duration.ofSeconds(29));

        TransferResult secondResult = transferService.send(
                "tx-2",
                "sender-1",
                "recipient-1",
                new BigDecimal("100.00"),
                "GBP",
                "EUR"
        );

        assertEquals(1, providerCallCount.get());
        assertEquals(new BigDecimal("1.20"), firstResult.exchangeRate());
        assertEquals(new BigDecimal("1.20"), secondResult.exchangeRate());
        assertEquals(new BigDecimal("800.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("240.00"), recipientAccount.getBalance());
    }

    @Test
    void shouldRefreshRateAtExactTtlExpiryBoundary() {
        User sender = new User("sender-1");
        Account senderAccount =
                new Account("GBP", new BigDecimal("1000.00"));
        sender.addAccount(senderAccount);

        User recipient = new User("recipient-1");
        Account recipientAccount =
                new Account("EUR", new BigDecimal("0.00"));
        recipient.addAccount(recipientAccount);

        UserRepository userRepository = userId -> {
            if ("sender-1".equals(userId)) {
                return sender;
            }
            if ("recipient-1".equals(userId)) {
                return recipient;
            }
            return null;
        };

        AtomicInteger providerCallCount = new AtomicInteger();

        ExchangeRateProvider exchangeRateProvider =
                (fromCurrency, toCurrency) -> {
                    int call = providerCallCount.incrementAndGet();
                    return call == 1
                            ? new BigDecimal("1.20")
                            : new BigDecimal("1.25");
                };

        MutableClock clock = new MutableClock(
                Instant.parse("2026-08-12T10:00:00Z")
        );

        TransferService transferService =
                new TransferService(
                        userRepository,
                        exchangeRateProvider,
                        Duration.ofSeconds(30),
                        clock
                );

        TransferResult firstResult = transferService.send(
                "tx-1",
                "sender-1",
                "recipient-1",
                new BigDecimal("100.00"),
                "GBP",
                "EUR"
        );

        clock.advance(Duration.ofSeconds(30));

        TransferResult secondResult = transferService.send(
                "tx-2",
                "sender-1",
                "recipient-1",
                new BigDecimal("100.00"),
                "GBP",
                "EUR"
        );

        assertEquals(2, providerCallCount.get());
        assertEquals(new BigDecimal("1.20"), firstResult.exchangeRate());
        assertEquals(new BigDecimal("1.25"), secondResult.exchangeRate());
        assertEquals(new BigDecimal("800.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("245.00"), recipientAccount.getBalance());
    }

    @Test
    void shouldUseStaleCachedRateWhenProviderFailsAfterExpiry() {
        User sender = new User("sender-1");
        Account senderAccount =
                new Account("GBP", new BigDecimal("1000.00"));
        sender.addAccount(senderAccount);

        User recipient = new User("recipient-1");
        Account recipientAccount =
                new Account("EUR", new BigDecimal("0.00"));
        recipient.addAccount(recipientAccount);

        UserRepository userRepository = userId -> {
            if ("sender-1".equals(userId)) {
                return sender;
            }
            if ("recipient-1".equals(userId)) {
                return recipient;
            }
            return null;
        };

        AtomicInteger providerCallCount = new AtomicInteger();

        ExchangeRateProvider exchangeRateProvider =
                (fromCurrency, toCurrency) -> {
                    int call = providerCallCount.incrementAndGet();

                    if (call == 1) {
                        return new BigDecimal("1.20");
                    }

                    throw new ExchangeRateException("provider unavailable");
                };

        MutableClock clock = new MutableClock(
                Instant.parse("2026-08-12T10:00:00Z")
        );

        TransferService transferService =
                new TransferService(
                        userRepository,
                        exchangeRateProvider,
                        Duration.ofSeconds(30),
                        clock
                );

        // First transfer populates the GBP -> EUR cache with 1.20.
        TransferResult firstResult = transferService.send(
                "tx-1",
                "sender-1",
                "recipient-1",
                new BigDecimal("100.00"),
                "GBP",
                "EUR"
        );

        // Move beyond the TTL so the cached rate becomes stale.
        clock.advance(Duration.ofSeconds(31));

        // Provider is attempted again, fails, and the stale 1.20 rate
        // should be used as the fallback.
        TransferResult secondResult = transferService.send(
                "tx-2",
                "sender-1",
                "recipient-1",
                new BigDecimal("100.00"),
                "GBP",
                "EUR"
        );

        assertEquals(2, providerCallCount.get());
        assertEquals(new BigDecimal("1.20"), firstResult.exchangeRate());
        assertEquals(new BigDecimal("1.20"), secondResult.exchangeRate());
        assertEquals(new BigDecimal("800.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("240.00"), recipientAccount.getBalance());
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        private MutableClock(Instant initialInstant) {
            this.currentInstant = initialInstant;
        }

        void advance(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            if (ZoneOffset.UTC.equals(zone)) {
                return this;
            }
            throw new UnsupportedOperationException(
                    "This test clock only supports UTC"
            );
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}