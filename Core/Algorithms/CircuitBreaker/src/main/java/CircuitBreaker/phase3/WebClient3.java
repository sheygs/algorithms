package CircuitBreaker.phase3;

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

class WebClient3 {

    /*
     * Circuit breaker policy:
     *
     * 3 failures within a rolling 20-second window -> OPEN.
     *
     * Interview probe:
     * This is "failures within a time window", NOT necessarily
     * "3 consecutive failures".
     */
    private static final int FAILURE_THRESHOLD = 3;
    private static final Duration FAILURE_WINDOW = Duration.ofSeconds(20);

    // How long an OPEN circuit rejects requests before allowing one recovery probe.
    private static final Duration COOLDOWN = Duration.ofSeconds(10);

    /*
     * Cache entries remain usable for 30 seconds.
     *
     * Interview probe:
     * TTL is a freshness/performance trade-off:
     * longer TTL -> fewer downstream calls but potentially staler data.
     */
    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    /*
     * One independent circuit per downstream service.
     *
     * ConcurrentHashMap makes lookup/creation thread-safe.
     * It does NOT make ServiceCircuit's internal mutable state thread-safe;
     * that is handled separately with synchronized methods.
     */
    private final Map<String, ServiceCircuit> circuits =
            new ConcurrentHashMap<>();

    /*
     * Shared response cache.
     *
     * ConcurrentHashMap allows multiple application threads to safely
     * access different cache entries concurrently.
     */
    private final Map<CacheKey, CacheEntry> cache =
            new ConcurrentHashMap<>();

    /*
     * Clock is injected for deterministic testing of:
     * - failure windows
     * - circuit cooldown
     * - cache expiry
     *
     * This avoids depending on Thread.sleep() in unit tests.
     */
    private final Clock clock;

    public WebClient3(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    public WebClient3() {
        this(Clock.systemUTC());
    }

    public Response execute(Request request, String requestKey) {

        if (request == null) {
            throw new IllegalArgumentException("request is not provided");
        }

        /*
         * requestKey identifies the logical request/resource being cached.
         *
         * Example:
         * ServiceB + user-123
         * ServiceB + user-456
         *
         * should be separate cache entries.
         */
        if (requestKey == null || requestKey.isBlank()) {
            throw new IllegalArgumentException("requestKey is not provided");
        }

        String service = request.getService();

        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("service name not provided");
        }

        Instant now = Instant.now(clock);

        /*
         * Cache key includes BOTH:
         *
         * service + requestKey
         *
         * Interview probe:
         * requestKey alone could collide across different downstream services.
         */
        CacheKey cacheKey = new CacheKey(service, requestKey);

        // ---------------- CACHE LOOKUP ----------------

        CacheEntry cached = cache.get(cacheKey);

        if (cached != null) {

            /*
             * Fresh cache hit:
             *
             * Return immediately without touching the circuit breaker
             * or making a downstream network call.
             */
            if (now.isBefore(cached.expiresAt)) {
                return cached.response;
            }

            /*
             * Cache entry exists but its TTL has expired.
             *
             * remove(key, value) is a conditional/atomic removal.
             *
             * Interview probe:
             * Why not just cache.remove(cacheKey)?
             *
             * Another thread may have replaced this stale entry with a fresh
             * one between our get() and remove().
             *
             * remove(cacheKey, cached) removes it ONLY if the map still
             * contains the exact entry we originally observed.
             */
            cache.remove(cacheKey, cached);
        }

        /*
         * VERY IMPORTANT INTERVIEW POINT:
         *
         * Circuit breaker is consulted ONLY after cache miss/expiry.
         *
         * A healthy cached response can therefore still be returned even
         * while the downstream circuit is OPEN.
         *
         * This reduces dependency load and improves availability.
         */
        ServiceCircuit circuit =
                circuits.computeIfAbsent(
                        service,
                        k -> new ServiceCircuit()
                );

        // ---------------- CIRCUIT ADMISSION ----------------

        /*
         * Atomic admission decision.
         *
         * Possible outcomes:
         *
         * NORMAL         -> ordinary CLOSED-state request
         * RECOVERY_PROBE -> one HALF_OPEN test request
         * REJECTED       -> fail fast
         */
        Admission admission =
                circuit.admit(now, FAILURE_WINDOW, COOLDOWN);

        /*
         * OPEN circuit or another request already performing
         * the HALF_OPEN probe -> reject without calling downstream.
         */
        if (admission == Admission.REJECTED) {
            throw new CircuitOpenException(service);
        }

        /*
         * VERY IMPORTANT:
         *
         * Network I/O occurs OUTSIDE synchronized methods.
         *
         * We synchronize state transitions, not remote calls.
         *
         * Otherwise one slow downstream request could hold the lock
         * and serialize all other callers.
         */
        Response response = null;
        Instant outcomeTime;

        try {

            response = call();

            if (response == null) {
                throw new IllegalStateException(
                        "downstream returned null response for service: "
                                + service
                );
            }

            /*
             * Measure time when the downstream result was actually observed,
             * rather than when the request started.
             *
             * This matters for slow downstream calls.
             */
            outcomeTime = Instant.now(clock);

        } catch (RuntimeException e) {

            /*
             * Both:
             * - downstream exceptions
             * - null downstream responses
             *
             * count as circuit failures in this implementation.
             */
            outcomeTime = Instant.now(clock);

            circuit.handleFailure(
                    admission,
                    outcomeTime,
                    FAILURE_WINDOW,
                    FAILURE_THRESHOLD
            );

            throw e;
        }

        // ---------------- APPLY RESULT ----------------

        if (response.getStatus() == 200) {

            /*
             * Successful recovery probe:
             *
             * HALF_OPEN -> CLOSED.
             *
             * A normal success leaves circuit failure history unchanged
             * because this implementation counts failures within a window
             * rather than consecutive failures.
             */
            circuit.handleSuccess(admission);

            /*
             * CACHE ONLY SUCCESSFUL RESPONSES.
             *
             * Interview probe:
             * Why not cache failures?
             *
             * Caching a temporary 500 could continue serving an error
             * even after the downstream service has recovered.
             */
            Instant expiresAt =
                    outcomeTime.plus(CACHE_TTL);

            CacheEntry cacheEntry =
                    new CacheEntry(response, expiresAt);

            /*
             * ConcurrentHashMap.put is thread-safe.
             *
             * If two threads simultaneously fetch the same uncached key,
             * both may call downstream and whichever finishes last can
             * overwrite the cache entry.
             *
             * This is safe but does NOT prevent a cache stampede.
             */
            cache.put(cacheKey, cacheEntry);

        } else {

            /*
             * Simplified failure policy:
             * every non-200 status counts as a downstream failure.
             *
             * Production implementations often count only:
             * - 5xx
             * - timeouts
             * - connection errors
             *
             * while ignoring expected client-side 4xx responses.
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
     * Admission records WHY the request was allowed.
     *
     * NORMAL:
     * ordinary request admitted while CLOSED.
     *
     * RECOVERY_PROBE:
     * the single request testing recovery after cooldown.
     *
     * REJECTED:
     * request must fail fast.
     */
    private enum Admission {
        NORMAL,
        RECOVERY_PROBE,
        REJECTED
    }

    /*
     * Circuit breaker state machine:
     *
     * CLOSED
     *   |
     *   | failure threshold reached
     *   v
     * OPEN
     *   |
     *   | cooldown expires
     *   v
     * HALF_OPEN
     *   |
     *   +-- probe succeeds --> CLOSED
     *   |
     *   +-- probe fails ----> OPEN
     */
    private enum CircuitState {
        OPEN,
        HALF_OPEN,
        CLOSED
    }

    private static class ServiceCircuit {

        /*
         * Sliding-window failure timestamps.
         *
         * ArrayDeque itself is NOT thread-safe.
         *
         * It is safe here because access occurs while holding this
         * ServiceCircuit's synchronized lock.
         */
        private final Deque<Instant> failureTimes =
                new ArrayDeque<>();

        private CircuitState state =
                CircuitState.CLOSED;

        /*
         * Timestamp representing when OPEN began.
         *
         * Used to determine:
         *
         * openedAt + cooldown
         */
        private Instant openedAt;

        private void pruneExpiredFailures(
                Instant now,
                Duration failureWindow
        ) {

            Instant cutOff =
                    now.minus(failureWindow);

            /*
             * Remove failures outside the active rolling window.
             *
             * Example:
             * threshold = 3 failures in 20 seconds.
             *
             * A failure from five minutes ago should no longer count.
             */
            failureTimes.removeIf(
                    t -> !t.isAfter(cutOff)
            );

            System.out.println(
                    "failureTimes state: " + failureTimes
            );
        }

        /*
         * Interview probe: why synchronized?
         *
         * Admission involves compound shared-state operations:
         *
         * read current state
         * -> inspect cooldown
         * -> potentially modify state
         * -> decide admission
         *
         * These steps must behave atomically.
         */
        synchronized Admission admit(
                Instant now,
                Duration failureWindow,
                Duration coolDown
        ) {

            pruneExpiredFailures(
                    now,
                    failureWindow
            );

            /*
             * CLOSED:
             *
             * Allow normal traffic.
             *
             * Although this method is synchronized, the lock is released
             * immediately after admission, so downstream requests themselves
             * remain concurrent.
             */
            if (state == CircuitState.CLOSED) {
                return Admission.NORMAL;
            }

            if (state == CircuitState.OPEN) {

                Instant coolDownEndsAt =
                        openedAt.plus(coolDown);

                /*
                 * Cooldown has not finished:
                 * reject immediately.
                 */
                if (now.isBefore(coolDownEndsAt)) {
                    return Admission.REJECTED;
                }

                /*
                 * Cooldown has expired.
                 *
                 * The first thread entering this synchronized block performs:
                 *
                 * OPEN -> HALF_OPEN
                 *
                 * and becomes the recovery probe.
                 *
                 * Synchronization guarantees two threads cannot both
                 * perform this transition simultaneously.
                 */
                state = CircuitState.HALF_OPEN;

                return Admission.RECOVERY_PROBE;
            }

            /*
             * HALF_OPEN means a recovery probe is already in progress.
             *
             * Reject every additional downstream request until the probe
             * completes.
             *
             * This prevents multiple simultaneous recovery probes.
             */
            return Admission.REJECTED;
        }

        /*
         * Failure processing is synchronized because:
         *
         * state
         * openedAt
         * failureTimes
         *
         * together form one logical state machine and must remain consistent.
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
             * This is fresh evidence that the downstream is still unhealthy.
             *
             * Therefore restart cooldown from outcomeTime.
             */
            if (admission == Admission.RECOVERY_PROBE) {

                state = CircuitState.OPEN;

                openedAt = outcomeTime;

                /*
                 * Reset previous failure history and begin a fresh
                 * post-probe failure period.
                 *
                 * Exact history-reset semantics are a design choice.
                 */
                failureTimes.clear();
                failureTimes.addLast(outcomeTime);

                return;
            }

            if (admission == Admission.NORMAL) {

                pruneExpiredFailures(
                        outcomeTime,
                        failureWindow
                );

                failureTimes.addLast(outcomeTime);

                /*
                 * Important concurrency edge case:
                 *
                 * Thread A, B and C may all have been admitted while CLOSED.
                 *
                 * A finishes and causes CLOSED -> OPEN.
                 *
                 * B may finish later with another failure.
                 *
                 * B should contribute to failure history, but should NOT
                 * reopen/restart the cooldown because its request was admitted
                 * before the circuit opened.
                 */
                if (
                        state == CircuitState.CLOSED
                                && failureTimes.size() >= failureThreshold
                ) {

                    state = CircuitState.OPEN;
                    openedAt = outcomeTime;
                }
            }
        }

        synchronized void handleSuccess(
                Admission admission
        ) {

            /*
             * Recovery probe succeeded:
             *
             * HALF_OPEN -> CLOSED.
             *
             * Clear failure history because the dependency has demonstrated
             * recovery.
             */
            if (admission == Admission.RECOVERY_PROBE) {

                failureTimes.clear();
                openedAt = null;
                state = CircuitState.CLOSED;
            }

            /*
             * NORMAL success does nothing.
             *
             * Why?
             *
             * We count failures within a rolling window, rather than
             * requiring consecutive failures.
             *
             * Therefore one successful request does not erase recent failures.
             */
        }
    }

    /*
     * Immutable cache value.
     *
     * Stores:
     *
     * - response
     * - absolute expiry timestamp
     */
    private static class CacheEntry {

        private final Response response;
        private final Instant expiresAt;

        CacheEntry(
                Response response,
                Instant expiresAt
        ) {

            this.response =
                    Objects.requireNonNull(
                            response,
                            "response is required"
                    );

            this.expiresAt =
                    Objects.requireNonNull(
                            expiresAt,
                            "expiresAt is required"
                    );
        }
    }

    /*
     * Composite cache key.
     *
     * service + requestKey uniquely identifies the cached downstream result.
     *
     * Interview probe:
     * Because this class is used as a HashMap/ConcurrentHashMap key,
     * equals() and hashCode() must agree.
     */
    private static class CacheKey {

        /*
         * Fields are final, making the key effectively immutable.
         *
         * This is important:
         * mutating a key after inserting it into a hash map can make
         * the entry effectively impossible to find again.
         */
        private final String service;
        private final String requestKey;

        CacheKey(
                String service,
                String requestKey
        ) {

            this.service =
                    Objects.requireNonNull(
                            service,
                            "service is required"
                    );

            this.requestKey =
                    Objects.requireNonNull(
                            requestKey,
                            "requestKey is required"
                    );
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (!(o instanceof CacheKey other)) {
                return false;
            }

            /*
             * Two keys are equal only when BOTH:
             *
             * service
             * requestKey
             *
             * match.
             */
            return Objects.equals(
                        service,
                        other.service
                    )
                    && Objects.equals(
                        requestKey,
                        other.requestKey
                    );
        }

        @Override
        public int hashCode() {

            /*
             * Must use the same fields as equals().
             *
             * Equal objects MUST produce equal hash codes.
             */
            return Objects.hash(
                    service,
                    requestKey
            );
        }
    }

    /**
     * Demo harness only.
     */
    public static void main(String[] args)
            throws InterruptedException {

        WebClient3 webClient =
                new WebClient3();

        Random random =
                new Random();

        for (int i = 0; i < 30; i++) {

            String service =
                    random.nextInt(2) == 0
                            ? "ServiceB"
                            : "ServiceC";

            Request request =
                    new Request(service);

            /*
             * Only five logical request keys are used repeatedly,
             * allowing the demo to produce cache hits.
             */
            String requestKey =
                    "request-" + (i % 5);

            try {

                Response response =
                        webClient.execute(
                                request,
                                requestKey
                        );

                System.out.println(
                        Instant.now()
                                + " -> "
                                + service
                                + " -> "
                                + requestKey
                                + " : "
                                + response
                );

            } catch (CircuitOpenException e) {

                System.out.println(
                        Instant.now()
                                + " -> exception: "
                                + e.getMessage()
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