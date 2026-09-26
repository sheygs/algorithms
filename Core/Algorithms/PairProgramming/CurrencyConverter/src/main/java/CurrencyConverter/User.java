package CurrencyConverter;

import java.util.HashMap;
import java.util.Map;

class User {
    private final Map<String, Account> accounts = new HashMap<>();

    public User(String id) {
    }

    public Account getAccount(String currency) {
        return accounts.get(currency);
    }

    public void addAccount(Account account) {
        accounts.put(account.getCurrency(), account);
    }
}
