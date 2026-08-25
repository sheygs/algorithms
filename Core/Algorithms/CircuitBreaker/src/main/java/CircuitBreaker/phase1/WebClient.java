package CircuitBreaker.phase1;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import CircuitBreaker.CircuitOpenException;
import CircuitBreaker.Request;
import CircuitBreaker.Response;

class WebClient {

    private static final int FAILURE_THRESHOLD = 3;
    private static final Duration FAILURE_WINDOW = Duration.ofSeconds(20);
    private static final Duration COOLDOWN = Duration.ofSeconds(10);

    private final Map<String, ServiceCircuit> circuits = new HashMap<>();

    public Response execute(Request request) {

        if (request == null) {
            throw new IllegalArgumentException("request is not provided");
        }

        String service = request.getService();
        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("service name not provided");
        }

        ServiceCircuit circuit = circuits.computeIfAbsent(service, k -> new ServiceCircuit());

        Instant now = Instant.now();
        ServiceCircuit.Admission decision = circuit.checkAccess(now, COOLDOWN);

        if (decision == ServiceCircuit.Admission.BLOCKED) {
            // fail fast
            throw new CircuitOpenException(service);
        }

        boolean recoveryAttempt = decision == ServiceCircuit.Admission.RECOVERY_PROBE;

        Response response;
        Instant outcomeTime;
        try {
            response = call();
            outcomeTime = Instant.now();
        } catch (RuntimeException e) {
            // timeouts / network errors count as failures too
            outcomeTime = Instant.now();
            circuit.onFailure(outcomeTime, FAILURE_WINDOW, FAILURE_THRESHOLD, recoveryAttempt);
            throw e; // breaker observes, doesn't swallow
        }

        if (response.getStatus() == 200) {
            circuit.onSuccess(recoveryAttempt);
        }

        if (response.getStatus() == 500) {
            circuit.onFailure(outcomeTime, FAILURE_WINDOW, FAILURE_THRESHOLD, recoveryAttempt);
        }

        return response;
    }

    /**
     * Supplied/opaque downstream simulation.
     * Do not move circuit-breaker logic into this method.
     */
    public Response call() {
        Random random = new Random();
        int rand = random.nextInt(2);

        Response response = new Response();
        response.setStatus(rand == 0 ? 200 : 500);

        return response;
    }

    private static class ServiceCircuit {

        private enum Admission { ALLOWED, RECOVERY_PROBE, BLOCKED }

        private final Deque<Instant> failureTimes = new ArrayDeque<>();
        private Instant blockedAt;

        /**
         * Single entry point for the open/half-open/closed decision.
         * Caller never inspects blockedAt directly.
         */
        private Admission checkAccess(Instant now, Duration cooldown) {
            if (blockedAt == null) {
                return Admission.ALLOWED;
            }

            Instant unblockedAt = blockedAt.plus(cooldown);
            if (now.isBefore(unblockedAt)) {
                return Admission.BLOCKED;
            }

            return Admission.RECOVERY_PROBE;
        }

        /**
         * Success path. Only a successful recovery probe closes the circuit again.
         */
        private void onSuccess(boolean recoveryAttempt) {
            if (recoveryAttempt) {
                reset();
            }
        }

        /**
         * Failure path (covers both HTTP 500 and thrown exceptions).
         * Records the failure, prunes the window, and trips the circuit if
         * this was a failed recovery probe or the threshold is breached.
         */
        private void onFailure(Instant failureTime, Duration failureWindow, int failureThreshold, boolean recoveryAttempt) {
            pruneExpiredFailures(failureTime, failureWindow);
            recordFailure(failureTime);

            if (recoveryAttempt || failureTimes.size() >= failureThreshold) {
                blockedAt = failureTime;
            }
        }

        private void reset() {
            blockedAt = null;
            failureTimes.clear();
        }

        private void pruneExpiredFailures(Instant now, Duration failureWindow) {
            Instant cutOff = now.minus(failureWindow);

            while (!failureTimes.isEmpty() && !failureTimes.peekFirst().isAfter(cutOff)) {
                failureTimes.removeFirst();
            }
        }

        private void recordFailure(Instant failureTime) {
            failureTimes.addLast(failureTime);
        }
    }

    /**
     * Demo harness only.
     */
    public static void main(String[] args) throws InterruptedException {

        WebClient webClient = new WebClient();
        Random random = new Random();

        for (int i = 0; i < 30; i++) {

            String service = random.nextInt(2) == 0 ? "ServiceB" : "ServiceC";

            Request request = new Request(service);
            try {
                Response response = webClient.execute(request);
                System.out.println(Instant.now() + " -> " + service + " : " + response);
            } catch (CircuitOpenException e) {
                System.out.println(Instant.now() + " -> " + "exception: " + e.getMessage());
            }
            catch (RuntimeException e) {
                System.out.println(Instant.now() + " -> downstream failure: " + e.getMessage());
            }

            Thread.sleep(2000);
        }
    }
}