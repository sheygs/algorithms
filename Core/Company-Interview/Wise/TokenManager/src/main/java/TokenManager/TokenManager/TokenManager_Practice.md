# Token Manager — Progressive Java Pair-Programming Practice

This README is a **from-scratch practice track** for rebuilding the Token Manager exercise we worked through together.

It is deliberately staged like a live pair-programming interview: **do not implement future stages early**. At each stage, read only the requirement, implement the smallest correct change, run the verification, and be able to explain the trade-off before moving on.

> **Evidence framing:** your workbook treats TTL/expiry, refresh-token handling, and explicit error handling as the reported Token Manager core. The exact original Wise starter and exact concurrency requirements are not public. The concurrency stages here are senior-level practice extensions, not a claim about the exact interview question.

---

## 0. What you are building

At a high level:

```text
Application
    |
    | getToken()
    v
TokenManager
    |
    +---- currentToken  <---- in-memory token cache
    |
    | only when no usable token exists
    v
fetchToken(...)
    |
    v
Simulated external authentication provider
```

The Token Manager owns the decision:

```text
NO TOKEN      -> obtain a token
VALID TOKEN   -> reuse the cached token
EXPIRED TOKEN -> refresh the token
```

The provider call is treated as an external dependency. Your implementation decides **when** it should be called.

---

# Practice rules

1. Start with the supplied starter only.
2. Do not add `Clock`, `volatile`, `synchronized`, or `CompletableFuture` before the stage that introduces them.
3. After every requirement change, rerun the relevant verification.
4. Always verify provider-call behaviour, not only returned token values.
5. For time semantics in this practice contract:
   - `now < expiresAt` -> token is valid.
   - `now == expiresAt` -> token is expired.
   - `now > expiresAt` -> token is expired.
6. `fetchToken(null)` means initial token acquisition in this local simulation.
7. `fetchToken(existingRefreshToken)` means refresh an expired token.
8. Never silently return an expired token after a provider/validation failure.

---

# Stage 0 — Starter code

Create `TokenManager.java`.

```java
import java.time.Instant;

public class TokenManager {

    // In-memory token cache.
    private TokenResponse currentToken;

    // Simulation-only state so provider calls generate visibly different tokens.
    private int tokenNumber = 0;

    public String getToken() {
        // TODO: implement progressively through this README.
        throw new UnsupportedOperationException("TODO");
    }

    public TokenResponse fetchToken(String refreshToken) {
        // SUPPLIED PROVIDER SIMULATION — treat as opaque during implementation.

        tokenNumber++;

        String accessToken = "access-token-" + tokenNumber;
        String newRefreshToken = "refresh-token-" + tokenNumber;
        Instant expiresAt = Instant.now().plusSeconds(10);

        if (refreshToken == null) {
            System.out.println("Obtaining initial token...");
        } else {
            System.out.println("Refreshing token using: " + refreshToken);
        }

        return new TokenResponse(
                accessToken,
                newRefreshToken,
                expiresAt
        );
    }

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            Instant expiresAt
    ) {}
}
```

### Understand before coding

- `currentToken` is your **in-memory cache**.
- `accessToken` is what callers need.
- `refreshToken` is used to obtain another access token after expiry.
- `expiresAt` is an absolute expiry timestamp.
- The simulated provider uses a 10-second TTL only so local practice is quick.

### Do not implement yet

- Error validation
- Injected `Clock`
- Concurrency
- `volatile`
- `synchronized`
- `CompletableFuture`
- Multi-pod/distributed coordination

---

# Stage 1 — Basic token lifecycle

## Interview requirement

> Implement `getToken()` so callers receive a usable access token while avoiding unnecessary calls to the external provider.

## Clarifications / contract

Implement these three states:

```text
1. currentToken == null
   -> fetchToken(null)
   -> cache the returned TokenResponse
   -> return its access token

2. currentToken exists AND now < expiresAt
   -> return cached access token
   -> DO NOT call fetchToken()

3. currentToken exists AND now >= expiresAt
   -> fetchToken(currentToken.refreshToken())
   -> replace the cached TokenResponse
   -> return the replacement access token
```

## TODO

Implement only `getToken()`.

Suggested pseudocode:

```text
getToken():

    if no current token:
        obtain token
        save it
        return access token

    if current token is still valid:
        return cached access token

    otherwise:
        refresh using current refresh token
        replace cached token
        return new access token
```

### Important implementation detail

When a provider response succeeds, remember:

```text
fetch -> cache -> return
```

If you forget to update `currentToken`, every later request may fetch/refresh again.

---

## Stage 1 manual verification

Temporarily add a `main()`:

```java
public static void main(String[] args) throws InterruptedException {
    TokenManager manager = new TokenManager();

    System.out.println("Call 1: " + manager.getToken());

    Thread.sleep(2_000);

    System.out.println("Call 2: " + manager.getToken());

    Thread.sleep(10_000);

    System.out.println("Call 3: " + manager.getToken());
}
```

Expected shape:

```text
Obtaining initial token...
Call 1: access-token-1

Call 2: access-token-1

Refreshing token using: refresh-token-1
Call 3: access-token-2
```

### What this proves

```text
Call 1 -> initial provider call exactly once
Call 2 -> cache hit, zero provider calls
Call 3 -> expired token, one refresh
```

### Interview checkpoint

Be able to explain:

- Why `currentToken` is a cache.
- Why the valid-token path must avoid the provider.
- Why an expired-token refresh must replace the cached response.
- Why equality with `expiresAt` is treated as expired in this practice contract.

---

# Stage 2 — Explicit error handling and response validation

## Interview requirement

> `fetchToken()` is an external dependency. It can fail or return malformed data. Do not cache or return an invalid token, and do not silently fall back to an expired token when refresh fails.

## Practice validation contract

Treat a provider response as invalid if:

```text
response == null
OR accessToken == null
OR accessToken is blank
OR expiresAt == null
```

For this practice version, throw:

```java
IllegalStateException("Invalid token response")
```

If `fetchToken()` itself throws a provider exception, allow it to propagate for now.

## TODO 1 — Extract validation

Add:

```java
private void validateTokenResponse(TokenResponse token) {
    // TODO
}
```

## TODO 2 — Change both provider paths

The safe order is now:

```text
fetch
  -> validate
  -> cache/publish
  -> return
```

Do this for both:

- initial acquisition
- expired-token refresh

Do **not** write directly into `currentToken` before validation succeeds.

---

## Stage 2 verification A — malformed response

For local testing only, temporarily make the simulation return something invalid, for example:

```java
return new TokenResponse(
        null,
        "refresh-token-1",
        Instant.now().plusSeconds(10)
);
```

Calling `getToken()` should result in:

```text
IllegalStateException: Invalid token response
```

Restore the normal provider simulation afterward.

## Stage 2 verification B — provider failure

For local testing only, temporarily make `fetchToken()` do:

```java
throw new RuntimeException("Auth provider unavailable");
```

Calling `getToken()` should surface that failure.

Restore the normal provider simulation afterward.

### Interview checkpoint

Be able to say:

> "I fetch into a temporary variable, validate it first, and only then publish it to `currentToken`. That prevents a malformed provider response from corrupting the cache."

---

# Stage 3 — TTL made deterministic with `Clock`

The Stage 1 logic uses `Instant.now()`, which is difficult to test at exact boundaries.

## Interview requirement

> Make time-dependent token expiry deterministic and testable without relying on `Thread.sleep()`.

## TTL model

The provider's TTL is converted into an absolute timestamp when the token is issued:

```text
issuedAt = 12:00:00
TTL      = 10 seconds
expiresAt = 12:00:10
```

The Token Manager then only needs to compare `now` with `expiresAt`.

---

## TODO 1 — inject `Clock`

Add:

```java
import java.time.Clock;
```

Add a field:

```java
private final Clock clock;
```

Add two constructors:

```java
public TokenManager() {
    // TODO: use the real system clock
}

public TokenManager(Clock clock) {
    // TODO: store injected clock
}
```

Normal usage should still support:

```java
new TokenManager();
```

Tests should support:

```java
new TokenManager(testClock);
```

## TODO 2 — replace direct wall-clock access

Replace time reads in your Token Manager logic with:

```java
clock.instant()
```

Also change the local simulated provider from:

```java
Instant.now().plusSeconds(10)
```

to:

```java
clock.instant().plusSeconds(10)
```

---

## Test helper — `MutableClock`

Create `MutableClock.java`:

```java
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

public final class MutableClock extends Clock {

    private volatile Instant currentTime;
    private final ZoneId zone;

    public MutableClock(Instant currentTime) {
        this(currentTime, ZoneId.of("UTC"));
    }

    private MutableClock(Instant currentTime, ZoneId zone) {
        this.currentTime = Objects.requireNonNull(currentTime);
        this.zone = Objects.requireNonNull(zone);
    }

    public void setInstant(Instant instant) {
        this.currentTime = Objects.requireNonNull(instant);
    }

    @Override
    public Instant instant() {
        return currentTime;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(currentTime, zone);
    }
}
```

`MutableClock` is test infrastructure, not Token Manager business logic.

---

## Stage 3 deterministic `main()`

Replace the sleep-based main with:

```java
public static void main(String[] args) {

    Instant start = Instant.parse("2026-08-10T12:00:00Z");

    MutableClock clock = new MutableClock(start);
    TokenManager manager = new TokenManager(clock);

    System.out.println("Time: " + clock.instant());
    System.out.println("Call 1: " + manager.getToken());

    clock.setInstant(
            Instant.parse("2026-08-10T12:00:09Z")
    );

    System.out.println("\nTime: " + clock.instant());
    System.out.println("Call 2: " + manager.getToken());

    clock.setInstant(
            Instant.parse("2026-08-10T12:00:10Z")
    );

    System.out.println("\nTime: " + clock.instant());
    System.out.println("Call 3: " + manager.getToken());
}
```

Expected:

```text
Time: 2026-08-10T12:00:00Z
Obtaining initial token...
Call 1: access-token-1

Time: 2026-08-10T12:00:09Z
Call 2: access-token-1

Time: 2026-08-10T12:00:10Z
Refreshing token using: refresh-token-1
Call 3: access-token-2
```

### What this proves

```text
12:00:09 < 12:00:10 -> valid cached token
12:00:10 < 12:00:10 -> false -> expired -> refresh
```

### Interview checkpoint

Be able to say:

> "I convert TTL into an absolute `expiresAt`, inject `Clock`, and treat the token as valid only while `clock.instant().isBefore(expiresAt)`. That lets me test the exact boundary deterministically without sleeping."

---

# Stage 4 — Expose the concurrency bug before fixing it

Do **not** add synchronization yet.

## Interview requirement

> Multiple callers may call `getToken()` concurrently. What happens if they all observe the same expired token?

## Goal

Reproduce this race:

```text
T1: sees expired token -> refresh
T2: sees expired token -> refresh
T3: sees expired token -> refresh
```

This is a **check-then-act race / refresh stampede**.

---

## TODO 1 — simulate slow provider I/O

For the local concurrency test, add a temporary delay only in the refresh branch of `fetchToken()`:

```java
if (refreshToken == null) {
    System.out.println("Obtaining initial token...");
} else {
    System.out.println("Refreshing token using: " + refreshToken);

    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
    }
}
```

The sleep is **not a solution**. It only makes the race easy to observe by simulating a slow remote provider.

---

## Stage 4 concurrent `main()`

```java
public static void main(String[] args) throws InterruptedException {

    Instant start = Instant.parse("2026-08-10T12:00:00Z");

    MutableClock clock = new MutableClock(start);
    TokenManager manager = new TokenManager(clock);

    System.out.println("Initial: " + manager.getToken());

    // Move exactly to expiry.
    clock.setInstant(
            Instant.parse("2026-08-10T12:00:10Z")
    );

    Thread thread1 = new Thread(() ->
            System.out.println("Thread 1: " + manager.getToken())
    );

    Thread thread2 = new Thread(() ->
            System.out.println("Thread 2: " + manager.getToken())
    );

    Thread thread3 = new Thread(() ->
            System.out.println("Thread 3: " + manager.getToken())
    );

    thread1.start();
    thread2.start();
    thread3.start();

    thread1.join();
    thread2.join();
    thread3.join();
}
```

Typical broken output:

```text
Obtaining initial token...
Initial: access-token-1
Refreshing token using: refresh-token-1
Refreshing token using: refresh-token-1
Refreshing token using: refresh-token-1
Thread 2: access-token-2
Thread 1: access-token-3
Thread 3: access-token-4
```

Thread ordering and token numbering are nondeterministic. The important observation is:

```text
"Refreshing token..." appears more than once.
```

### Explain the bug

The unsafe compound operation is:

```text
check expiry
   -> decide refresh required
   -> perform provider call
   -> publish replacement token
```

Individual reads/writes are not enough. The whole decision must be coordinated.

---

# Stage 5 — Simplest correct concurrency solution: `synchronized`

## Interview requirement

> Ensure concurrent callers do not all refresh the same expired token. Start with the simplest correct implementation.

## TODO

Make the whole `getToken()` method synchronized:

```java
public synchronized String getToken() {
    // your existing Stage 3 implementation
}
```

Do not redesign anything else yet.

---

## Stage 5 verification

Rerun the same three-thread test.

Expected shape:

```text
Obtaining initial token...
Initial: access-token-1
Refreshing token using: refresh-token-1
Thread 1: access-token-2
Thread 2: access-token-2
Thread 3: access-token-2
```

Thread print order may differ.

The required invariant is:

```text
3 concurrent callers
-> exactly 1 refresh
-> all return the same replacement token
```

---

## Stage 5 interview Q&A checkpoint

### Why did `synchronized` fix the race?

Because only one thread can execute `getToken()` on the same `TokenManager` instance at a time. The first thread refreshes and updates `currentToken`; later threads enter afterward, re-evaluate the state naturally, see the new valid token, and return it.

### What is locked?

With:

```java
public synchronized String getToken()
```

the monitor of that specific `TokenManager` instance (`this`) is locked.

### What happens to the other threads?

They block waiting for the same instance monitor.

### Main limitation

The lock covers the entire method, including:

```java
fetchToken(...)
```

which represents slow network I/O.

Therefore:

```text
T1 owns lock -> provider takes 2 seconds
T2 waits
T3 waits
T4 waits
```

Even valid-token callers are serialized through the monitor.

### Production-improvement answer

> "I would start with `synchronized` because it is simple and correct. Its limitation is coarse locking, especially holding the monitor across provider I/O. I would next introduce a lock-free valid-token fast path with `volatile`, synchronize only the slow path, and double-check after acquiring the lock. If refresh contention remained important, I would move to single-flight."

---

# Stage 6 — `volatile` fast path + synchronized slow path + double-check

This stage improves the coarse synchronized version while keeping correctness.

## Goal

Normal valid-token requests should avoid taking the lock:

```text
VALID TOKEN
-> fast path
-> no synchronized block
-> return immediately

NO TOKEN / EXPIRED TOKEN
-> slow path
-> coordinate refresh
```

---

## TODO 1 — make `currentToken` visible to lock-free readers

Change:

```java
private TokenResponse currentToken;
```

to:

```java
private volatile TokenResponse currentToken;
```

### Why `volatile`?

Because `currentToken` will now be read outside the synchronized block. `volatile` gives visibility of a newly published token to other threads.

### Why `volatile` is NOT enough by itself

`volatile` does not make this compound operation atomic:

```text
read token
-> check expiry
-> decide to refresh
-> call provider
-> publish token
```

Multiple threads can still see the same expired volatile value and all decide to refresh.

---

## TODO 2 — create the lock-free fast path

Start `getToken()` with a local snapshot:

```java
TokenResponse latestToken = currentToken;
```

If it exists and is valid, return it immediately.

Do not enter a synchronized block for the valid-token case.

---

## TODO 3 — synchronize the slow path

If the first check fails, enter:

```java
synchronized (this) {
    // TODO
}
```

Inside that block:

1. Read `currentToken` again.
2. Check validity again.
3. Only if it is still missing/expired should this thread obtain or refresh.
4. Validate before assigning to `currentToken`.

### Why the second check is mandatory

While your thread was waiting for the lock, another thread may already have refreshed:

```text
T1 sees expired
T2 sees expired

T1 acquires lock
T1 refreshes -> token-2
T1 releases lock

T2 acquires lock
T2 MUST read/check currentToken again
T2 sees token-2 valid
T2 returns token-2
```

Without the second check, T2 can perform an unnecessary second refresh.

---

## Stage 6 scaffold

Fill the TODOs yourself:

```java
public String getToken() {

    TokenResponse latestToken = currentToken;

    // TODO: fast path — return if latestToken is valid.

    synchronized (this) {

        // TODO: reread currentToken.

        // TODO: double-check validity.

        // TODO: if no token, obtain + validate + publish.

        // TODO: otherwise refresh + validate + publish.
    }
}
```

## Stage 6 verification

Rerun the Stage 4 three-thread test.

You should still get exactly one refresh and one shared replacement token.

### Remaining limitation

Even though valid-token reads are now lock-free, the slow path still performs:

```java
fetchToken(...)
```

**while holding the synchronized lock**.

That is the reason for the next stage.

---

# Stage 7 — `CompletableFuture` single-flight

This is the advanced in-process refinement.

## Goal

For one missing/expired token generation:

```text
ONE thread starts provider work
OTHER concurrent callers share/wait for that same in-flight work
```

The expensive provider call should happen **outside** the synchronized block.

---

# Stage 7A — represent an in-flight token fetch

Add:

```java
import java.util.concurrent.CompletableFuture;
```

Add the field:

```java
private CompletableFuture<TokenResponse> tokenFetchInFlight;
```

Keep:

```java
private volatile TokenResponse currentToken;
```

### Meaning

```text
currentToken
-> last successfully published token

tokenFetchInFlight == null
-> nobody is currently obtaining/refreshing

tokenFetchInFlight != null
-> an obtain/refresh is currently in progress
```

`tokenFetchInFlight` does not need to be `volatile` in this design because all inspection/creation/cleanup of that field happens under the same synchronized monitor.

Do not change provider execution yet.

---

# Stage 7B — establish owner vs follower under a short lock

Before the synchronized block, declare:

```java
CompletableFuture<TokenResponse> future;
boolean owner;
String refreshToken = null;
```

Inside the synchronized slow path:

1. Re-read `currentToken`.
2. Double-check whether it has become valid.
3. If `tokenFetchInFlight == null`:
   - create a new `CompletableFuture<TokenResponse>`;
   - publish it to `tokenFetchInFlight`;
   - mark this thread as `owner = true`;
   - capture the refresh token to use (`null` for first acquisition).
4. Otherwise:
   - mark this thread as `owner = false`.
5. Copy the shared `tokenFetchInFlight` reference into local variable `future`.
6. Exit the synchronized block.

## Stage 7B scaffold

```java
CompletableFuture<TokenResponse> future;
boolean owner;
String refreshToken = null;

synchronized (this) {

    // TODO: reread currentToken.

    // TODO: double-check valid-token case.

    if (tokenFetchInFlight == null) {
        // TODO: create/publish ONE shared future.
        // TODO: mark this thread OWNER.
        // TODO: capture null or current refresh token.
    } else {
        // TODO: mark this thread FOLLOWER.
    }

    // TODO: every owner/follower gets the SAME local future reference.
}
```

### Invariant to understand

```text
T1 -> OWNER    -> Future A
T2 -> FOLLOWER -> Future A
T3 -> FOLLOWER -> Future A
```

Never:

```text
T1 -> Future A
T2 -> Future B
T3 -> Future C
```

The synchronized block is now only responsible for **admission/ownership**, not the slow provider call.

---

# Stage 7C — owner performs provider call outside the lock

After leaving the synchronized block:

- If this thread is the owner:
  1. call `fetchToken(refreshToken)`;
  2. validate the response;
  3. publish it to `currentToken`;
  4. complete the shared future successfully.
- If the provider/validation fails:
  1. complete the same future exceptionally.
- Followers do not call the provider.
- Owner and followers both obtain the result from the same local `future`.

## Stage 7C scaffold

```java
if (owner) {
    try {
        // TODO: fetch outside synchronized block.
        // TODO: validate.
        // TODO: publish currentToken.
        // TODO: complete future successfully.
    } catch (Exception e) {
        // TODO: complete future exceptionally.
    }
}

// TODO: owner and followers consume the same future result.
```

A simple blocking consumer is:

```java
future.join()
```

### Important error-semantics note

`CompletableFuture.join()` wraps an exceptional completion in `CompletionException`.

If the interview contract requires callers to receive a specific original provider exception type, explicitly translate/unwrap it rather than accidentally changing the API's error contract.

---

# Stage 7D — clean up the in-flight future

If `tokenFetchInFlight` is never cleared, the next token expiry could incorrectly reuse an old completed future.

## Requirement

After the owner's provider attempt finishes — success **or failure** — clear the shared in-flight state.

Do this in a `finally` block.

Because `tokenFetchInFlight` is monitor-guarded, perform cleanup under the same synchronized lock.

Use a defensive identity check:

```java
if (tokenFetchInFlight == future) {
    tokenFetchInFlight = null;
}
```

### Why `finally`?

```text
SUCCESS -> complete future -> clear in-flight state
FAILURE -> complete future exceptionally -> clear in-flight state
```

A failed operation must not permanently poison all future token requests.

### Why clearing the field does not break followers

Followers already copied the shared future into their own local variable:

```text
T2.future ----\
T3.future -----+--> Future A
T4.future ----/
```

Clearing the manager field does not destroy the `Future A` object those callers already reference.

---

# Stage 8 — Final single-flight verification

Use the same `MutableClock` and slow-provider simulation.

## Verification 1 — concurrent expired-token refresh

At `12:00:00` obtain `access-token-1`.

Move the clock to `12:00:10`.

Start three threads simultaneously.

Expected shape:

```text
Obtaining initial token...
Initial: access-token-1
Refreshing token using: refresh-token-1
Thread 1: access-token-2
Thread 2: access-token-2
Thread 3: access-token-2
```

Required assertions:

```text
"Refreshing token..." appears exactly once.
All concurrent callers receive the same replacement token.
```

---

## Verification 2 — later expiry creates a NEW single-flight operation

The token created at `12:00:10` expires at `12:00:20`.

Move the clock to:

```java
clock.setInstant(
        Instant.parse("2026-08-10T12:00:20Z")
);
```

Then call `getToken()` again.

Expected shape:

```text
Refreshing token using: refresh-token-2
After second expiry: access-token-3
```

This proves the previous `tokenFetchInFlight` was cleaned up and a later expiry can create a fresh future.

---

## Verification 3 — concurrent initial acquisition

Create a fresh `TokenManager` where `currentToken == null`.

Start several threads calling `getToken()` together.

Expected:

```text
ONE "Obtaining initial token..." provider call
ALL callers receive the same initial access token
```

The same single-flight mechanism should protect both:

```text
first token acquisition
AND
expired-token refresh
```

---

## Verification 4 — failed owner refresh

Temporarily make the provider fail during refresh.

Expected behaviour:

```text
ONE provider attempt
shared future completes exceptionally
all overlapping followers observe that failed shared operation
tokenFetchInFlight is cleared afterward
later calls are able to attempt another refresh
```

Remember that `join()` exposes exceptional completion as a `CompletionException` unless you translate it.

---

## Verification 5 — malformed owner response

Temporarily return an invalid `TokenResponse` during refresh.

Expected:

```text
validation fails
invalid response is NOT assigned to currentToken
future completes exceptionally
in-flight state is cleaned up
```

---

# Final implementation progression to remember

```text
STAGE 1
Basic lifecycle
no token -> obtain
valid -> reuse
expired -> refresh

        |
        v

STAGE 2
Error handling
fetch -> validate -> publish

        |
        v

STAGE 3
Deterministic TTL
Clock + absolute expiresAt

        |
        v

STAGE 4
Expose race
multiple threads -> multiple refreshes

        |
        v

STAGE 5
synchronized whole method
simple + correct exactly-one-refresh

        |
        v

STAGE 6
volatile + double-check
lock-free valid-token fast path
synchronized slow path

        |
        v

STAGE 7
CompletableFuture single-flight
short ownership lock
provider call outside lock
followers share one future
cleanup after success/failure
```

---

# Core concurrency concepts for this Token Manager

## 1. `synchronized`

Purpose:

```text
mutual exclusion around the refresh decision
```

Interview sentence:

> "I use synchronization to make expiry checking, refresh admission, and token publication mutually exclusive so concurrent callers cannot all refresh the same expired token."

---

## 2. `volatile`

Purpose:

```text
visibility of currentToken for lock-free readers
```

Not sufficient for:

```text
compound check -> decide -> refresh -> publish atomicity
```

Interview sentence:

> "`volatile` gives me visibility for the fast path, but it does not make the multi-step refresh operation atomic, so I still need coordination for the slow path."

---

## 3. Double-check after acquiring the lock

Purpose:

```text
another thread may have refreshed while this thread waited
```

Interview sentence:

> "I re-read and re-check the token after acquiring the lock because the state may have changed while I was waiting; without the second check I could perform an unnecessary duplicate refresh."

---

## 4. Single-flight

Purpose:

```text
one expensive obtain/refresh operation
many concurrent callers share its result
```

Interview sentence:

> "If holding the lock around provider I/O becomes a throughput concern, I use single-flight so exactly one refresh is in progress and concurrent callers share that same in-flight result instead of duplicating provider calls."

---

## 5. `CompletableFuture`

Purpose in this design:

```text
represent the one shared in-flight token operation
```

Important distinction:

`CompletableFuture` alone does **not** establish single-flight. Creation/publication of the shared future must itself be coordinated atomically, otherwise two threads could create two different futures and two different provider calls.

Interview sentence:

> "The `CompletableFuture` represents the shared in-flight refresh. I still briefly synchronize ownership so only one future/provider call is created; followers then join the same future."

---

# Interview Q&A — likely concurrency follow-ups

### What happens if 20 threads call `getToken()` at expiry?

Without coordination, all 20 can observe the same expired token and all call the provider. With synchronization or single-flight, only the intended refresh occurs and the other callers reuse/share its result.

### Why did you start with `synchronized`?

It is the smallest, easiest-to-reason-about correct solution. Correctness comes before optimizing contention.

### What exactly does `public synchronized getToken()` lock?

The intrinsic monitor of that specific `TokenManager` instance (`this`). It does not lock the whole JVM.

### What happens to the other threads?

They block until the monitor becomes available, then re-evaluate the token state.

### What is the limitation of synchronizing the whole method?

The lock is held during the slow provider call, serializing callers and potentially reducing throughput.

### Why is `volatile` not enough?

It solves visibility, not atomicity of the compound expiry-check/refresh/publish operation.

### Why double-check after acquiring the lock?

The token may have been refreshed by another thread while the current thread was waiting.

### Why move to single-flight?

To keep exactly-one-refresh behaviour while avoiding a design where the expensive external call itself must be performed while holding the coordination lock.

### Does `CompletableFuture` automatically prevent duplicate refreshes?

No. The code must still atomically establish who creates/publishes the one shared future.

### What if the provider call fails in single-flight?

Complete the shared future exceptionally so overlapping followers observe the same failed operation, then clear in-flight state so a later call can attempt again according to the agreed error/retry policy.

---

# Production discussion — concise interview answer

> "I would start with the simplest synchronized solution because it gives clear correctness and prevents a refresh stampede. Its main limitation is coarse locking: the monitor can be held across a slow provider call, so callers may queue behind network I/O. I would first add a volatile fast path and double-check inside a synchronized slow path so valid-token reads do not lock. If refresh contention is still meaningful, I would move to a single-flight design with one shared in-flight future and perform the provider call outside the coordination lock. For production I would also add provider timeouts, deliberate retry/error semantics, metrics and tracing, avoid logging token secrets, consider a small pre-expiry safety margin if required, and only introduce cross-instance coordination if the token ownership requirement actually spans multiple pods."

---

# What not to over-engineer in the interview

Unless specifically requested, do not immediately add:

- Redis or another distributed cache
- Distributed locks
- Multi-pod token ownership
- Refresh jitter
- Pre-expiry refresh margins
- Background refresh schedulers
- Retry frameworks
- Complex executor pools
- `AtomicReference`-based state machines
- Reactive streams

Start with the visible requirement, establish correctness, test it, then evolve only when the interviewer introduces the next constraint.

---

# Final readiness checklist

Before considering this Token Manager mastered, rebuild it from the Stage 0 starter without looking at your completed solution and verify that you can:

- [ ] Explain the Token Manager architecture and in-memory cache.
- [ ] Implement initial token acquisition.
- [ ] Reuse a valid cached token with zero provider calls.
- [ ] Refresh exactly at/after expiry.
- [ ] Explain TTL versus absolute `expiresAt`.
- [ ] Validate provider responses before publishing them.
- [ ] Surface provider failures instead of returning an expired token.
- [ ] Inject `Clock` and test the exact expiry boundary without sleeping.
- [ ] Reproduce the concurrent refresh stampede intentionally.
- [ ] Fix it first with `synchronized`.
- [ ] Explain what is locked, what waiting threads do, and why coarse locking is a limitation.
- [ ] Implement `volatile` fast path + synchronized slow path + double-check.
- [ ] Explain why `volatile` alone does not fix the race.
- [ ] Implement owner/follower single-flight with one shared `CompletableFuture`.
- [ ] Keep the provider call outside the ownership lock.
- [ ] Complete the future exceptionally on provider/validation failure.
- [ ] Clean `tokenFetchInFlight` after success and failure.
- [ ] Verify a later expiry can create a new in-flight refresh.
- [ ] Explain the progression from simple correctness to production-oriented concurrency without over-engineering.

# Architecture

```text
                        getToken()
                            │
                            ▼
                ┌──────────────────────┐
                │ In-memory token cache│
                │    currentToken      │
                └──────────┬───────────┘
                           │
                ┌──────────┴───────────┐
                │                      │
             NO TOKEN              TOKEN EXISTS
                │                      │
                │                      ▼
                │               Has it expired?
                │                  /       \
                │                NO         YES
                │                │           │
                │                ▼           │
                │          return cached     │
                │          access token      │
                │                            │
                └──────────────┬─────────────┘
                               │
                               ▼
                       fetchToken(...)
                               │
                               ▼
                        Auth Provider
                               │
                               ▼
                        TokenResponse
                               │
                               ▼
                     update currentToken
                               │
                               ▼
                     return accessToken
```

```text
                     ┌───────────────┐
                     │     Clock     │
                     │               │
                     │ "what is now?"│
                     └───────┬───────┘
                             │
                             ▼
Application ────────> TokenManager
                         │
                         │
                 ┌───────┴─────────┐
                 │                 │
                 ▼                 ▼
          currentToken        fetchToken()
             cache            fake provider
```

## Limitations and Production Fixes

The synchronized `getToken()` version is correct because it prevents multiple threads from refreshing an expired token at the same time. The main limitation is that the lock covers the entire method, including the slow external `fetchToken()` call. That means all callers are serialized and may block behind network I/O, even though most calls may only need to return a valid cached token.

For production, I’d first reduce the lock scope by using a lock-free fast path for valid tokens with `volatile`, then synchronize only the refresh decision and double-check after acquiring the lock. If refresh contention is still significant, I’d move to a single-flight approach, for example with a shared `CompletableFuture`, so only one refresh happens while other callers share that same in-flight result. I’d also add proper metrics, timeouts, failure handling, and consider multi-instance coordination only if the business requires a globally shared token.
