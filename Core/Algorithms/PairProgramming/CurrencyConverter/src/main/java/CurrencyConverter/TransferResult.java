package CurrencyConverter;

import java.math.BigDecimal;

record TransferResult(
        String transferId,
        TransferStatus status,
        BigDecimal sentAmount,
        String sourceCurrency,
        BigDecimal receivedAmount,
        String targetCurrency,
        BigDecimal exchangeRate) {
}
