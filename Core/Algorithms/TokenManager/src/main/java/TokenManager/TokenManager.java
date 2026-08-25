package TokenManager.TokenManager;

import java.time.Instant;
import java.util.Objects;
import java.time.Clock;

class TokenManager {
    // in-memory cache
    private TokenResponse currentToken;
    private final Clock clock;

    // supplied simulation so we can see whether a new token was actually fetched.
    private int tokenNumber = 0;

    // production
    public TokenManager() {
        this(Clock.systemUTC());
    }

    // tests
    public TokenManager(Clock clock) {
      this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    /**
     *
     * synchronized means only one thread at a time can execute
     * getToken() on the same TokenManager object
     * Only one thread should refresh, other threads wait until the lock has been released
     */
    public synchronized String getToken() {
        // case 1: Non-existent token
        // fetch -> cache -> return token
        if (currentToken == null) {
            TokenResponse token = fetchToken(null);

            validateTokenResponse(token);

            currentToken = token;

            return currentToken.accessToken();
        }

        // case 2: read existing & valid token from cache
        if (clock.instant().isBefore(currentToken.expiresAt())) {
            return currentToken.accessToken();
        }

        // case 3: expired token
        // refresh -> replace cache -> return new token
        TokenResponse refreshedToken = fetchToken(currentToken.refreshToken());

        validateTokenResponse(refreshedToken);

        currentToken = refreshedToken;

        return currentToken.accessToken();
    }

    public TokenResponse fetchToken(String refreshToken) {
        // DO NOT MODIFY: SUPPLIED SIMULATION
        tokenNumber++;

        String accessToken = "access-token-" + tokenNumber;
        String newRefreshToken = "refresh-token-" + tokenNumber;

        Instant expiresAt = clock.instant().plusSeconds(10);

        if (refreshToken == null) {
          System.out.println("Obtaining initial token...");
        }
        else {
            System.out.println("Refreshing token using: " + refreshToken);

            // simulates slow refresh for testing concurrency
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        // simulates provider outage and malformed/invalid token

        // return new TokenResponse(
        //         null,
        //         "refresh-token-1",
        //         Instant.now().plusSeconds(10)
        // );

        // throw new RuntimeException("Auth provider unavailable");

        return new TokenResponse(
                accessToken,
                newRefreshToken,
                expiresAt
        );
    }

    private void validateTokenResponse(TokenResponse token) {
        if (token == null ||
            token.accessToken() == null ||
            token.accessToken().isBlank() ||
            token.expiresAt() == null) {
            throw new IllegalStateException("Invalid token response");
        }
    }

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            Instant expiresAt) {
    }

    public static void main(String[] args) throws InterruptedException {

        // Old Approach using Thread.sleep()
        // TokenManager manager = new TokenManager();
        // System.out.println("Call 1: " + manager.getToken());
        // Thread.sleep(3000);
        // System.out.println("Call 2: " + manager.getToken());
        // Thread.sleep(8000);
        // System.out.println("Call 3: " + manager.getToken());

        Instant start = Instant.parse("2026-08-10T12:00:00Z");

        MutableClock clock = new MutableClock(start);

        TokenManager manager = new TokenManager(clock);

        /* concurrency simulation */

        // STEP 1: obtain initial token at 12:00:00
        System.out.println("Initial: " + manager.getToken());

        // STEP 2: move time exactly to expiry
        clock.setInstant(Instant.parse("2026-08-10T12:00:10Z"));

        // STEP 3: multiple callers arrive after expiry
        int numThreads = 3;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
           int threadId = i + 1;

           threads[i] = new Thread(() -> {
              System.out.println("Thread " + threadId + ": " + manager.getToken());
           });

           threads[i].start();
        }

        for (Thread t: threads) {
            t.join();
        }

        /* end concurrency simulation */

        // 1. No token yet -> fetch initial token
        // System.out.println("Time: " + clock.instant());
        // System.out.println("Call 1: " + manager.getToken());

        // 2. Move to 1 second before expiry
        // clock.setInstant(Instant.parse("2026-08-10T12:00:09Z"));

        // System.out.println("\nTime: " + clock.instant());
        // System.out.println("Call 2: " + manager.getToken());

        // 3. Move to exact expiry boundary
        // clock.setInstant(Instant.parse("2026-08-10T12:00:10Z"));

        // System.out.println("\nTime: " + clock.instant());
        // System.out.println("Call 3: " + manager.getToken());
    }
}