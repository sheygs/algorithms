package CurrencyConverter;

import java.math.BigDecimal;

interface ExchangeRateProvider {
    // Treat this as a supplied external API.
    BigDecimal getRate(String from, String to) throws ExchangeRateException;
}
