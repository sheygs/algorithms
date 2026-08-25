import java.io.*;
import java.util.*;
import java.text.*;
import java.util.stream.*;
import java.net.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;


abstract class Request {

    String host;

    abstract Response call();
}

class Response {

    int status;

    String body;
}


class Clock {

    private static int currentTimeInMinutes = 0;


    protected static int getCurrentTimeInMinutes() {
        return currentTimeInMinutes;
    }


    protected static void setCurrentTime(int mins) {
        currentTimeInMinutes = mins;
    }
}



class WebClient {

    private static final int FAILURE_THRESHOLD = 3;
    private static final int FAILURE_WINDOW_MINUTES = 10;
    private static final int BLOCK_DURATION_MINUTES = 5;

    /*
     * Each downstream host gets its own circuit state.
     *
     * service-b.com -> CircuitState
     * service-c.com -> CircuitState
     */
    private final Map<String, CircuitState> circuits = new HashMap<>();


    private static class CircuitState {

        // Times at which failures happened.
        Deque<Integer> failureTimes = new ArrayDeque<>();

        // null means the circuit is not blocked/open.
        Integer blockedAt = null;
    }


    private int getCurrentTimeInMinutes() {
        return Clock.getCurrentTimeInMinutes();
    }


    public Response execute(Request request) throws Exception {

        int now = getCurrentTimeInMinutes();

        CircuitState state = circuits.computeIfAbsent(request.host,host -> new CircuitState());

        /*
         * 1. Check whether this service is currently blocked.
         */
        if (state.blockedAt != null) {

            int blockedFor = now - state.blockedAt;

            if (blockedFor < BLOCK_DURATION_MINUTES) {

                // IMPORTANT:
                // request.call() must NOT be invoked here.
                throw new Exception(
                        "Circuit breaker open for host: " + request.host
                );
            }

            /*
             * Cooldown has finished.
             *
             * Allow requests again and start with a fresh failure window.
             */
            state.blockedAt = null;
            state.failureTimes.clear();
        }


        /*
         * 2. Remove failures outside the rolling 10-minute window.
         */
        removeOldFailures(state, now);


        /*
         * 3. Actually call the downstream service.
         */
        try {

            Response response = request.call();

            /*
             * Treat server errors as failures.
             */
            if (response != null && response.status >= 500 && response.status <= 599) {
                recordFailure(state, now);
            }

            return response;

        } catch (Exception e) {

            /*
             * Timeout/network exceptions also count as failures.
             */
            recordFailure(state, now);

            // Preserve the original downstream exception.
            throw e;
        }
    }

    private void recordFailure(
            CircuitState state,
            int now
    ) {

        /*
         * First discard failures that are no longer
         * inside the rolling 10-minute window.
         */
        removeOldFailures(state, now);

        /*
         * Record the new failure.
         */
        state.failureTimes.addLast(now);


        /*
         * Trip/open the circuit after failure #3.
         */
        if (state.failureTimes.size() >= FAILURE_THRESHOLD) {

            state.blockedAt = now;
        }
    }


    private void removeOldFailures(
            CircuitState state,
            int now
    ) {

        while (!state.failureTimes.isEmpty()
                && state.failureTimes.peekFirst() < now - FAILURE_WINDOW_MINUTES) {

            state.failureTimes.removeFirst();
        }
    }

    static Request buildRequest(String host) {

        return new Request() {

            {
                this.host = host;
            }

            @Override
            Response call() {

                /*
                 * In the real HackerRank harness,
                 * this would probably simulate or perform
                 * the downstream API request.
                 */

                Response response = new Response();

                response.status = 200;
                response.body = "OK";

                return response;
            }
        };
    }

    public static void main(String[] args) throws Exception {

        WebClient webClient = new WebClient();


        Request requestToServiceB =
                buildRequest("service-b.com");

        Response responseFromServiceB =
                webClient.execute(requestToServiceB);

        System.out.println("response from service B: " + responseFromServiceB);


        Request requestToServiceC =
                buildRequest("service-c.com");

        Response responseFromServiceC =
                webClient.execute(requestToServiceC);

        System.out.println("response from service C: " + responseFromServiceC);
    }
}