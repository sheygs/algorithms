package TokenManager.TokenManager;

import java.time.Instant;
import java.util.Objects;
import java.time.Clock;

class TokenManagerV2 {

    // in-mem cache
    private volatile TokenResponse currentToken;
    private final Clock clock;

    // supplied simulation so we can see whether a new token was actually fetched.
    private int tokenNumber = 0;

    // production
    public TokenManagerV2() {
        this(Clock.systemUTC());
    }

    // tests
    public TokenManagerV2(Clock clock) {
      this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    // volatile fast path + synchronized slow path + double-check
    public String getToken() {

        // fast path: valid token
        TokenResponse token = currentToken;

        if (token != null && clock.instant().isBefore(token.expiresAt())){
            return token.accessToken();
        }

        // slow path
        synchronized (this) {
            // IMPORTANT: check again after acquiring the lock
            token = currentToken;

            if (token != null && clock.instant().isBefore(token.expiresAt())) {
                return token.accessToken();
            }

            // Non-existent token
            // fetch -> cache -> return token
            if (token == null) {
                TokenResponse newToken = fetchToken(null);

                validateTokenResponse(newToken);

                currentToken = newToken;

                return newToken.accessToken();
            }

            // expired token
            // refresh -> replace cache -> return new token
            TokenResponse refreshedToken = fetchToken(token.refreshToken());

            validateTokenResponse(refreshedToken);

            currentToken = refreshedToken;

            return refreshedToken.accessToken();
        }

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
            System.out.println("Refreshing expired token using: " + refreshToken);

            // slow refresh simulation for testing concurrency
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

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

        Instant start = Instant.parse("2026-08-10T12:00:00Z");

        MutableClock clock = new MutableClock(start);

        TokenManagerV2 manager = new TokenManagerV2(clock);

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

        /* end concurrency simulation  */
    }
}