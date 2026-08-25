package CircuitBreaker.phase2;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import CircuitBreaker.CircuitOpenException;
import CircuitBreaker.Request;
import CircuitBreaker.Response;

class WebClient2 {

    // Interview probe:
    // Why 3 failures within 20 seconds rather than 3 failures forever?
    // The sliding failure window prevents old failures from permanently
    // contributing toward opening the circuit.
    private static final int FAILURE_THRESHOLD = 3;
    private static final Duration FAILURE_WINDOW = Duration.ofSeconds(20);

    // Once OPEN, requests fail fast until this cooldown expires.
    private static final Duration COOLDOWN = Duration.ofSeconds(10);

    /*
     * Interview probe: circuit granularity.
     *
     * We maintain one independent circuit per downstream service.
     * Failure of ServiceB should not prevent calls to ServiceC.
     *
     * ConcurrentHashMap makes circuit lookup/creation thread-safe,
     * but DOES NOT make ServiceCircuit's internal state transitions thread-safe.
     */
    private final Map<String, ServiceCircuit> circuits = new ConcurrentHashMap<>();

    /*
     * Clock is injected so time-dependent behaviour such as:
     * - failure window expiry
     * - cooldown expiry
     * can be tested deterministically.
     */
    private final Clock clock;

    public WebClient2(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    public WebClient2() {
        this(Clock.systemUTC());
    }

    public Response execute(Request request) {

        if (request == null) {
            throw new IllegalArgumentException("request is not provided");
        }

        String service = request.getService();

        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("service name not provided");
        }

        /*
         * Interview probe: computeIfAbsent is atomic for circuit creation.
         *
         * Two threads requesting the same service should not create and
         * independently use two different circuit breakers.
         */
        ServiceCircuit circuit =
            circuits.computeIfAbsent(service, k -> new ServiceCircuit());

        Instant now = Instant.now(clock);

        /*
         * ADMISSION CHECK
         *
         * This decision must be atomic because several threads could arrive
         * exactly when OPEN's cooldown expires.
         *
         * We want exactly ONE thread to become the HALF_OPEN recovery probe.
         */
        Admission admission =
            circuit.admit(now, FAILURE_WINDOW, COOLDOWN);

        /*
         * OPEN or HALF_OPEN requests that are not the selected probe
         * fail immediately without calling the downstream service.
         *
         * This is the "fail fast" behaviour of a circuit breaker.
         */
        if (admission == Admission.REJECTED) {
            throw new CircuitOpenException(service);
        }

        /*
         * VERY IMPORTANT INTERVIEW POINT:
         *
         * The network call is OUTSIDE synchronized methods.
         *
         * Synchronizing the remote call would serialize all callers and cause
         * slow network latency to block unrelated requests.
         *
         * Only short shared-state transitions are synchronized.
         */
        Response response = null;
        Instant outcomeTime;

        try {
            response = call();

            if (response == null) {
                throw new IllegalStateException(
                    "downstream returned null response for service: " + service
                );
            }

            /*
             * Record failure/success using the time the OUTCOME was observed,
             * not the time the request began.
             *
             * Important when downstream calls are slow.
             */
            outcomeTime = Instant.now(clock);

        } catch (RuntimeException e) {

            outcomeTime = Instant.now(clock);

            /*
             * Exceptions are treated as downstream failures too.
             *
             * Interview probe:
             * production implementations may distinguish between failures
             * that should affect circuit health and client/application errors
             * that should not.
             */
            circuit.handleFailure(
                admission,
                outcomeTime,
                FAILURE_WINDOW,
                FAILURE_THRESHOLD
            );

            throw e;
        }

        /*
         * Atomically apply the outcome after the network call.
         *
         * Admission and completion are separate critical sections because
         * the network call must remain concurrent.
         */
        if (response.getStatus() == 200) {

            circuit.handleSuccess(admission);

        } else {

            /*
             * Simplified policy:
             * every non-200 response counts as a failure.
             *
             * Interview probe:
             * in production you might count only 5xx/timeouts/connectivity
             * failures while ignoring expected 4xx responses.
             */
            circuit.handleFailure(
                admission,
                outcomeTime,
                FAILURE_WINDOW,
                FAILURE_THRESHOLD
            );
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

    /*
     * Admission describes WHY a request was allowed through.
     *
     * NORMAL         = normal CLOSED-state traffic
     * RECOVERY_PROBE = single HALF_OPEN test request
     * REJECTED       = fail fast
     *
     * Remembering the admission type matters when the outcome comes back.
     */
    private enum Admission {
        NORMAL,
        RECOVERY_PROBE,
        REJECTED
    }

    /*
     * Standard circuit-breaker state machine:
     *
     * CLOSED -> normal traffic
     * OPEN -> reject traffic during cooldown
     * HALF_OPEN -> allow one recovery probe
     */
    private enum CircuitState {
        OPEN,
        HALF_OPEN,
        CLOSED
    }

    private static class ServiceCircuit {

        /*
         * ArrayDeque is NOT thread-safe.
         *
         * Access is safe here because all mutation occurs while holding
         * this ServiceCircuit's intrinsic lock via synchronized methods.
         */
        private final Deque<Instant> failureTimes = new ArrayDeque<>();

        private CircuitState state = CircuitState.CLOSED;

        /*
         * Time at which the circuit most recently entered OPEN.
         * Used to calculate cooldown expiry.
         */
        private Instant openedAt;

        private void pruneExpiredFailures(
            Instant now,
            Duration failureWindow
        ) {

            Instant cutOff = now.minus(failureWindow);

            /*
             * Keep only failures inside the active sliding window.
             *
             * Example:
             * threshold = 3 failures in 20 seconds.
             *
             * Three failures spread over ten minutes should NOT open
             * the circuit.
             */
            failureTimes.removeIf(t -> !t.isAfter(cutOff));

            System.out.println("failureTimes state: " + failureTimes);
        }

        /*
         * Interview probe: why synchronized?
         *
         * Admission is a compound read-modify-write operation:
         *
         * read state
         * -> check cooldown
         * -> possibly transition OPEN -> HALF_OPEN
         * -> decide who gets admitted
         *
         * These operations must happen atomically.
         */
        synchronized Admission admit(
            Instant now,
            Duration failureWindow,
            Duration coolDown
        ) {

            pruneExpiredFailures(now, failureWindow);

            /*
             * CLOSED:
             * ordinary traffic is allowed concurrently.
             *
             * The synchronization only protects this short decision;
             * callers release the lock before making the network request.
             */
            if (state == CircuitState.CLOSED) {
                return Admission.NORMAL;
            }

            if (state == CircuitState.OPEN) {

                Instant coolDownEndsAt = openedAt.plus(coolDown);

                // Still inside cooldown -> fail fast.
                if (now.isBefore(coolDownEndsAt)) {
                    return Admission.REJECTED;
                }

                /*
                 * Cooldown expired.
                 *
                 * The first thread holding this lock performs:
                 *
                 * OPEN -> HALF_OPEN
                 *
                 * and becomes the single recovery probe.
                 *
                 * Because this method is synchronized, another thread
                 * cannot simultaneously make the same transition.
                 */
                state = CircuitState.HALF_OPEN;

                return Admission.RECOVERY_PROBE;
            }

            /*
             * HALF_OPEN means a recovery probe is already in flight.
             *
             * Reject everyone else until its result is known.
             *
             * This avoids a "probe stampede".
             */
            return Admission.REJECTED;
        }

        /*
         * Outcome processing is synchronized because state, openedAt and
         * failureTimes form one logical state machine and must be updated
         * consistently.
         */
        synchronized void handleFailure(
            Admission admission,
            Instant outcomeTime,
            Duration failureWindow,
            int failureThreshold
        ) {

            /*
             * RECOVERY PROBE FAILURE:
             *
             * HALF_OPEN -> OPEN
             *
             * Restart cooldown from when the failed probe outcome was
             * actually observed.
             */
            if (admission == Admission.RECOVERY_PROBE) {

                state = CircuitState.OPEN;

                // Recovery failed, so begin a fresh cooldown.
                openedAt = outcomeTime;

                /*
                 * Begin a fresh failure history for the new OPEN period.
                 *
                 * Exact failure-history policy is a design choice;
                 * interviewers may ask why we clear it here.
                 */
                failureTimes.clear();
                failureTimes.addLast(outcomeTime);

                return;
            }

            if (admission == Admission.NORMAL) {

                /*
                 * Maintain only failures within the active window before
                 * recording the newest failure.
                 */
                pruneExpiredFailures(outcomeTime, failureWindow);

                failureTimes.addLast(outcomeTime);

                /*
                 * Only CLOSED -> OPEN here.
                 *
                 * Important concurrency edge case:
                 * NORMAL requests admitted while CLOSED may still be in flight
                 * after another request has already opened the circuit.
                 *
                 * Their failures may contribute to history, but they should NOT
                 * restart openedAt/cooldown after the circuit is already OPEN.
                 */
                if (
                    state == CircuitState.CLOSED &&
                    failureTimes.size() >= failureThreshold
                ) {

                    state = CircuitState.OPEN;
                    openedAt = outcomeTime;
                }
            }
        }

        synchronized void handleSuccess(Admission admission) {

            /*
             * RECOVERY PROBE SUCCESS:
             *
             * HALF_OPEN -> CLOSED
             *
             * The downstream is considered healthy again, so reset the
             * circuit's failure state.
             */
            if (admission == Admission.RECOVERY_PROBE) {

                failureTimes.clear();
                openedAt = null;
                state = CircuitState.CLOSED;
            }

            /*
             * NORMAL success does nothing.
             *
             * This implementation uses a failure-count-within-time-window
             * policy rather than "consecutive failures", so one normal
             * success does not erase previous failures.
             *
             * Interviewers may ask you to explain this design choice.
             */
        }
    }

    /**
     * Demo harness only.
     */
    public static void main(String[] args) throws InterruptedException {

        WebClient2 webClient = new WebClient2();
        Random random = new Random();

        for (int i = 0; i < 30; i++) {

            String service =
                random.nextInt(2) == 0 ? "ServiceB" : "ServiceC";

            Request request = new Request(service);

            try {

                Response response = webClient.execute(request);

                System.out.println(
                    Instant.now() + " -> " + service + " : " + response
                );

            } catch (CircuitOpenException e) {

                System.out.println(
                    Instant.now() + " -> exception: " + e.getMessage()
                );

            } catch (RuntimeException e) {

                System.out.println(
                    Instant.now()
                    + " -> downstream failure: "
                    + e.getMessage()
                );
            }

            Thread.sleep(2000);
        }
    }
}