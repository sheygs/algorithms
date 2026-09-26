package CurrencyConverter;

import java.math.BigDecimal;

class Account {

    private final String currency;
    private BigDecimal balance;

    public Account(String currency, BigDecimal balance) {
        this.currency = currency;
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
