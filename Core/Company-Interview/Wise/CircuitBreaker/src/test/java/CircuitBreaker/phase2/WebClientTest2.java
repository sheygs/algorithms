package CircuitBreaker.phase2;

import CircuitBreaker.CircuitOpenException;
import CircuitBreaker.Request;
import CircuitBreaker.Response;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebClientTest2 {

    /**
     * A clock that we control manually.
     *
     * This allows us to test:
     *
     * openedAt = 10:00:00
     * cooldown = 10 seconds
     *
     * then move directly to:
     *
     * now = 10:00:10
     *
     * without Thread.sleep(10_000).
     */
    static class MutableClock extends Clock {

        private volatile Instant instant;
        private final ZoneId zone;

        MutableClock(Instant instant, ZoneId zone) {
            this.instant = Objects.requireNonNull(instant);
            this.zone = Objects.requireNonNull(zone);
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    /**
     * Fake WebClient2 used only for testing.
     *
     * Instead of calling the random downstream implementation,
     * we control:
     *
     * 1. which status code is returned
     * 2. how many times downstream was called
     * 3. whether downstream should block
     */
    static class TestWebClient extends WebClient2 {

        private final AtomicInteger callCount = new AtomicInteger();

        private volatile int status = 500;

        private volatile CountDownLatch downstreamEntered;
        private volatile CountDownLatch releaseDownstream;

        TestWebClient(Clock clock) {
            super(clock);
        }

        void setStatus(int status) {
            this.status = status;
        }

        int getCallCount() {
            return callCount.get();
        }

        /**
         * Configure call() so that once a thread reaches downstream:
         *
         * - downstreamEntered is signalled
         * - the thread waits on releaseDownstream
         */
        void blockDownstream(
                CountDownLatch downstreamEntered,
                CountDownLatch releaseDownstream) {

            this.downstreamEntered = downstreamEntered;
            this.releaseDownstream = releaseDownstream;
        }

        @Override
        public Response call() {

            // Count every actual downstream call.
            callCount.incrementAndGet();

            /*
             * Tell the test that a request has successfully
             * reached downstream.
             */
            if (downstreamEntered != null) {
                downstreamEntered.countDown();
            }

            /*
             * Optionally block the downstream request.
             *
             * This is important for the recovery test because
             * we want the circuit to remain HALF_OPEN while the
             * other 19 threads attempt admission.
             */
            if (releaseDownstream != null) {
                try {
                    releaseDownstream.await();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    throw new RuntimeException(e);
                }
            }

            Response response = new Response();
            response.setStatus(status);

            return response;
        }
    }

    @Test
    void exactlyOneRecoveryProbeShouldReachDownstream() throws Exception {

        // ----------------------------------------------------
        // 1. Create deterministic clock
        // ----------------------------------------------------

        MutableClock clock = new MutableClock(
                Instant.parse("2026-01-01T10:00:00Z"),
                ZoneOffset.UTC
        );

        TestWebClient client = new TestWebClient(clock);

        Request request = new Request("ServiceB");


        // ----------------------------------------------------
        // 2. Force ServiceB OPEN
        // ----------------------------------------------------

        client.setStatus(500);

        client.execute(request); // failure 1
        client.execute(request); // failure 2
        client.execute(request); // failure 3 -> OPEN

        /*
         * Three requests should have reached downstream.
         */
        assertEquals(3, client.getCallCount());


        // ----------------------------------------------------
        // 3. Move exactly to cooldown boundary
        // ----------------------------------------------------

        clock.advance(Duration.ofSeconds(10));

        /*
         * Recovery will succeed eventually.
         *
         * But we are going to block it before it can return,
         * keeping the circuit HALF_OPEN.
         */
        client.setStatus(200);


        // ----------------------------------------------------
        // 4. Prepare recovery downstream blocking
        // ----------------------------------------------------

        CountDownLatch recoveryEntered =
                new CountDownLatch(1);

        CountDownLatch releaseRecovery =
                new CountDownLatch(1);

        client.blockDownstream(
                recoveryEntered,
                releaseRecovery
        );


        // ----------------------------------------------------
        // 5. Prepare 20 concurrent callers
        // ----------------------------------------------------

        int numberOfThreads = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfThreads);

        /*
         * ready:
         *
         * ensures all 20 worker threads have been created and
         * are waiting before we release them.
         */
        CountDownLatch ready =
                new CountDownLatch(numberOfThreads);

        /*
         * startGate:
         *
         * all 20 threads wait here.
         *
         * countDown() releases them together.
         */
        CountDownLatch startGate =
                new CountDownLatch(1);

        /*
         * rejectedLatch:
         *
         * we expect exactly 19 requests to be rejected.
         */
        CountDownLatch rejectedLatch =
                new CountDownLatch(numberOfThreads - 1);

        /*
         * done:
         *
         * lets the test know when all 20 tasks have completed.
         */
        CountDownLatch done =
                new CountDownLatch(numberOfThreads);

        AtomicInteger rejected =
                new AtomicInteger();


        // ----------------------------------------------------
        // 6. Submit 20 requests
        // ----------------------------------------------------

        for (int i = 0; i < numberOfThreads; i++) {

            executor.submit(() -> {

                // This worker is ready.
                ready.countDown();

                try {

                    // Wait until the test releases everybody.
                    startGate.await();

                    client.execute(request);
                }
                catch (CircuitOpenException e) {

                    rejected.incrementAndGet();

                    rejectedLatch.countDown();
                }
                catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                }
                finally {

                    done.countDown();
                }
            });
        }


        // ----------------------------------------------------
        // 7. Wait until all 20 workers are ready
        // ----------------------------------------------------

        assertTrue(
                ready.await(2, TimeUnit.SECONDS),
                "All worker threads should become ready"
        );


        // ----------------------------------------------------
        // 8. Release all 20 at roughly the same time
        // ----------------------------------------------------

        startGate.countDown();


        // ----------------------------------------------------
        // 9. Wait until one request reaches recovery call()
        // ----------------------------------------------------

        assertTrue(
                recoveryEntered.await(2, TimeUnit.SECONDS),
                "Exactly one recovery request should reach downstream"
        );


        /*
         * At this point that one recovery request is blocked
         * inside call().
         *
         * Its admission already changed:
         *
         * OPEN -> HALF_OPEN
         *
         * Therefore every other request should see HALF_OPEN
         * and fail fast.
         */


        // ----------------------------------------------------
        // 10. Wait for the other 19 to be rejected
        // ----------------------------------------------------

        assertTrue(
                rejectedLatch.await(2, TimeUnit.SECONDS),
                "The other 19 requests should be rejected"
        );


        // ----------------------------------------------------
        // 11. Assert concurrency invariant
        // ----------------------------------------------------

        /*
         * Initial failures:
         *
         * 3 downstream calls
         *
         * Recovery:
         *
         * 1 downstream call
         *
         * Total:
         *
         * 4
         */
        assertEquals(4, client.getCallCount());

        assertEquals(19, rejected.get());


        // ----------------------------------------------------
        // 12. Allow recovery request to complete
        // ----------------------------------------------------

        releaseRecovery.countDown();


        // ----------------------------------------------------
        // 13. Wait for everything to finish
        // ----------------------------------------------------

        assertTrue(
                done.await(2, TimeUnit.SECONDS),
                "All concurrent requests should finish"
        );

        executor.shutdown();

        assertTrue(
                executor.awaitTermination(2, TimeUnit.SECONDS)
        );
    }
}