package CurrencyConverter;

import java.math.BigDecimal;
import java.time.Instant;

record CachedRate(BigDecimal rate, Instant expiresAt) {
}
