package CircuitBreaker;

public class CircuitOpenException extends RuntimeException {

    public CircuitOpenException(String service) {
        super("Circuit is open for service: " + service);
    }
}