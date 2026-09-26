package CurrencyConverter;

interface UserRepository {
    // Returns null when the user does not exist.
    User findById(String userId);
}
