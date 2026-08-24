package TokenManager.TokenManager;

import java.time.Instant;
import java.util.Objects;
import java.time.Clock;
import java.util.concurrent.CompletableFuture;

class TokenManagerV3 {

    // in-mem cache
    private volatile TokenResponse currentToken;
    private CompletableFuture<TokenResponse> tokenFetchInFlight;
    private final Clock clock;

    // supplied simulation so we can see whether a new token was actually fetched.
    private int tokenNumber = 0;

    // production
    public TokenManagerV3() {
        this(Clock.systemUTC());
    }

    // tests
    public TokenManagerV3(Clock clock) {
      this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    // single flight with completableFuture
    public String getToken() {

        // fast path: valid token
        TokenResponse latestToken = currentToken;

        if (latestToken != null && clock.instant().isBefore(latestToken.expiresAt())) {
           return latestToken.accessToken();
        }


        CompletableFuture<TokenResponse> future;
        boolean owner;
        String refreshToken = null;

        // slow path
        synchronized (this) {
           // Double-check:
           // another thread may have refreshed while we waited for the lock
           latestToken = currentToken;

           if (latestToken != null && clock.instant().isBefore(latestToken.expiresAt())) {
              return latestToken.accessToken();
           }

           if (tokenFetchInFlight == null) {
              // No fetch is currently happening.
              // This thread becomes the OWNER.
              tokenFetchInFlight = new CompletableFuture<>();
              owner = true;
              refreshToken = latestToken == null ? null : latestToken.refreshToken();
           } else {
              // Somebody else is already fetching.
              // This thread becomes a FOLLOWER.
              owner = false;
           }
            // Both owner and followers receive
            // the SAME future object.
            future = tokenFetchInFlight;
        }

        if (owner) {
           // perform fetch and complete future
           try {
               TokenResponse token = fetchToken(refreshToken);

               validateTokenResponse(token);

               currentToken = token;

               future.complete(token);

            } catch (Exception e) {
               future.completeExceptionally(e);
            }
              finally {
               synchronized (this) {
                     if (tokenFetchInFlight == future) {
                        tokenFetchInFlight = null;
                     }
               }
            }

        }

        return future.join().accessToken();
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

        TokenManagerV3 manager = new TokenManagerV3(clock);

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