# LC 1797 - Design Authentication Manager

## Problem

There is an authentication system that works with authentication tokens. For each session, the user receives a new authentication token that expires in `timeToLive` seconds. If the token is reused before it expires, its `timeToLive` is renewed based on the current time, and it will expire `timeToLive` seconds after the renewal time.

Implement the `AuthenticationManager` class:

- `AuthenticationManager(int timeToLive)` — constructs the `AuthenticationManager` and sets the `timeToLive`.
- `void generate(String tokenId, int currentTime)` — generates a new token with the given `tokenId` at the given `currentTime` (in seconds).
- `void renew(String tokenId, int currentTime)` — renews the **unexpired** token with the given `tokenId` at the given `currentTime`. If there is no unexpired token with the given `tokenId`, the request is ignored and nothing happens.
- `int countUnexpiredTokens(int currentTime)` — returns the number of unexpired tokens at the given `currentTime`.

**Note:** If a token expires at time `t`, and another action (`renew` or `countUnexpiredTokens`) happens at time `t`, the expiration takes place **before** the other action is evaluated.

## Example 1

```text
Input
["AuthenticationManager", "renew", "generate", "countUnexpiredTokens", "generate", "renew", "renew", "countUnexpiredTokens"]
[[5], ["aaa", 1], ["aaa", 2], [6], ["bbb", 7], ["aaa", 8], ["bbb", 10], [15]]

Output
[null, null, null, 1, null, null, null, 0]

Explanation
AuthenticationManager authenticationManager = new AuthenticationManager(5); // timeToLive = 5 seconds
authenticationManager.renew("aaa", 1);              // No token "aaa" exists yet -> ignored
authenticationManager.generate("aaa", 2);            // Token "aaa" generated, expires at 7
authenticationManager.countUnexpiredTokens(6);       // "aaa" unexpired -> returns 1
authenticationManager.generate("bbb", 7);            // Token "bbb" generated, expires at 12
authenticationManager.renew("aaa", 8);               // "aaa" expired at 7 (8 >= 7) -> ignored
authenticationManager.renew("bbb", 10);              // "bbb" unexpired -> renewed, now expires at 15
authenticationManager.countUnexpiredTokens(15);      // "bbb" expires exactly at 15, "aaa" expired -> returns 0
```

## Constraints

- `1 <= timeToLive <= 10^8`
- `1 <= currentTime <= 10^8`
- `1 <= tokenId.length <= 5`
- `tokenId` consists only of lowercase letters.
- All calls to `generate` will contain unique values of `tokenId`.
- The values of `currentTime` across all function calls are strictly increasing.
- At most `2000` calls will be made to all functions combined.

## Approach

Use a **HashMap** to associate each token with its expiration time:

```text
tokenId -> expirationTime
```

For example, with `timeToLive = 5`, calling `generate("abc", 2)` stores:

```text
abc -> 7        // 2 + 5 = 7
```

### Generate

Look up the token first:

```text
expirationTime = tokens.get(tokenId)
```

- If the token exists and `expirationTime > currentTime`, it is still active, so generation is ignored.
- If the token does not exist, or it already expired, store a new expiration time:

```text
expirationTime = currentTime + timeToLive
```

For the original LeetCode problem, every call to `generate` is guaranteed to use a unique `tokenId`, so the existing-token check is not required by the problem. It is a defensive extension in this implementation that also allows an expired token ID to be generated again safely.

### Renew

A token can only be renewed if it is still valid:

```text
expirationTime > currentTime
```

If the token does not exist, or `expirationTime <= currentTime`, the renewal is ignored. Otherwise, replace its expiration time with:

```text
currentTime + timeToLive
```

### Count Unexpired Tokens

A token is valid when:

```text
expirationTime > currentTime
```

Iterate through the stored expiration times and count those satisfying this condition.

## Boundary Condition

The most important detail in this problem:

```text
expirationTime == currentTime   ->  expired
expirationTime >  currentTime   ->  valid
```

**Walkthrough:**

```text
TTL = 5
generate("abc", 2)      // abc expires at 7

currentTime = 6         // 7 > 6  -> still valid
currentTime = 7         // 7 > 7 is false -> already expired, cannot be renewed
```

**Extended example:**

```text
TTL = 5

generate("A", 1)   // A -> 6
generate("B", 2)   // B -> 7

count(5)
A: 6 > 5 ✓
B: 7 > 5 ✓
Result = 2

renew("A", 5)      // A -> 10 (5 + 5)
                   // B -> 7  (unchanged)

count(7)
A: 10 > 7 ✓
B: 7 > 7  ✗   (B expired exactly at time 7)
Result = 1
```

## Complexity

Let `n` be the number of tokens stored.

| Operation              | Time | Space |
| ---------------------- | ---: | ----: |
| `generate`             | O(1) |  O(1) |
| `renew`                | O(1) |  O(1) |
| `countUnexpiredTokens` | O(n) |  O(1) |
| Overall storage        |    — |  O(n) |

## Key Takeaway

The problem simplifies significantly when each token is represented by its **expiration timestamp** rather than actively tracked over time. Every operation then reduces to comparing:

```text
expirationTime > currentTime
```

The main edge case to remember: a token is already expired when `expirationTime == currentTime`.

## Topics

- Hash Table
- Linked List
- Design
- Doubly-Linked List
- Ordered Set

## Observation

The `tokenId == null || tokenId.isBlank()` validation in `generate` and `renew` is useful defensive validation outside the LeetCode constraints, although the challenge itself guarantees valid lowercase token IDs.

## Production Limitations & Recommended Fixes

The `HashMap` solution is appropriate for the coding problem, but a real authentication service has additional requirements around durability, concurrency, cleanup, scalability, time handling, and security.

| Limitation                                 | Why it matters in production                                                                                                | Recommended fix                                                                                                                                     |
| ------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| In-memory `HashMap` state                  | Tokens disappear when the application restarts and are not shared across multiple service instances.                        | Store token/session state in a shared store such as Redis.                                                                                          |
| `HashMap` is not thread-safe               | Multiple request threads can access and modify the map concurrently.                                                        | For a single JVM, use a concurrency-safe structure and atomic operations. For a distributed service, use atomic operations in the shared store.     |
| Read-check-write operations are not atomic | `get -> check -> put` can race. One concurrent renewal can overwrite another renewal with an older expiration time.         | Use atomic operations such as `ConcurrentHashMap.compute(...)` in memory, or an atomic Redis operation/Lua script in a distributed deployment.      |
| Expired entries remain in the map          | Expired tokens are ignored when counting but never removed, so memory usage can grow continuously.                          | Use TTL-based eviction. Redis supports native key expiration.                                                                                       |
| `countUnexpiredTokens` is O(n)             | Every count scans all stored tokens, which becomes expensive as the token population grows.                                 | Avoid synchronous full scans; if an exact count is required, maintain an expiry-aware index/counter such as a Redis sorted set.                     |
| Caller supplies `currentTime`              | In production, security decisions should not depend on client-controlled time.                                              | Use server-controlled time, preferably through `Clock` and `Instant` so tests can still use a fixed clock.                                          |
| Time is represented with `int`             | Primitive integers are less expressive and are not a good general-purpose representation for real timestamps and durations. | Use `Duration` for TTL and `Instant` for expiration timestamps.                                                                                     |
| Caller supplies the token ID               | Predictable or user-chosen bearer tokens would be unsafe.                                                                   | Generate cryptographically secure random tokens on the server; for opaque bearer tokens, store a hash where appropriate rather than the raw secret. |
| Renewal can continue indefinitely          | A stolen token could potentially remain valid as long as it keeps being renewed before expiry.                              | If required by the security model, enforce both an idle TTL and a maximum absolute session lifetime.                                                |

### 1. Shared / Externalized State

The current state lives inside one JVM:

```text
Application instance
       |
       v
    HashMap
```

An application restart clears the map, and two application instances would each have different token state. A production deployment would typically externalize the state into a shared store:

```text
                 Load Balancer
                /             \
               v               v
        Auth Service A    Auth Service B
                \             /
                 \           /
                    v
                  Redis
               token state
                 + TTL
```

Redis is a natural fit for opaque session/token state because all application instances can access the same data and Redis can expire keys automatically after their TTL. If the system requires the token state itself to survive Redis node failures or restarts, use an appropriate Redis high-availability/persistence configuration, or another durable datastore depending on the required guarantees.

### 2. Concurrency and Atomicity

Replacing `HashMap` with `ConcurrentHashMap` only makes individual map operations thread-safe. The business operation is still a compound operation:

```text
get
 ↓
check expiration
 ↓
calculate new expiration
 ↓
put
```

Another thread can execute between those steps.

For example, suppose token `ABC` expires at `110` and `timeToLive = 10`:

```text
Thread A: renew("ABC", 100) -> wants expiry 110
Thread B: renew("ABC", 105) -> wants expiry 115
```

If Thread B writes `115` first and Thread A writes `110` afterward, the newer renewal is lost and the final expiration incorrectly moves backward to `110`.

For an in-memory implementation, the complete read-check-write operation should be atomic, for example with `ConcurrentHashMap.compute(...)`. In a distributed deployment, the atomicity must be enforced by the shared datastore because separate service instances do not share JVM locks.

### 3. Expired-Token Cleanup

`countUnexpiredTokens` correctly ignores expired tokens, but it does not remove them. Over time the map can therefore contain a large number of dead entries:

```text
ABC -> expired
DEF -> expired
GHI -> expired
XYZ -> active
```

A TTL-aware store solves this more naturally. With Redis, each token can be stored with an expiration so expired state is automatically removed.

### 4. Counting at Scale

The current implementation scans every stored expiration time:

```text
countUnexpiredTokens = O(n)
```

That is completely acceptable under the problem constraint of at most `2000` calls, but it may be too expensive with millions of active and expired sessions. In production, first question whether an exact synchronous count is actually required. If it is, maintain an expiry-aware index or counter rather than scanning all token records on every request.

### 5. Server-Controlled Time

Passing `currentTime` into each method is required by the coding problem, but a production service should determine the current time itself. A testable Java design can inject a `Clock`:

```java
private final Clock clock;
private final Duration timeToLive;
```

Production uses the real system clock, while unit tests can use `Clock.fixed(...)` for deterministic time-based tests.

### 6. Token Security

The LeetCode API supplies `tokenId`, but a real authentication service should generate unpredictable token values using a cryptographically secure random source. Depending on the token design, the service should also consider storing only a hash of an opaque bearer token so a datastore compromise does not immediately expose usable credentials.

### 7. Session Lifetime

The current renewal rule implements a sliding expiration: every successful renewal resets the expiry to `currentTime + timeToLive`. That can allow a session to continue indefinitely. If the security requirements call for it, combine the sliding/idle TTL with an absolute expiration time that renewal cannot extend.

## Production Interview Summary

> The `HashMap` solution is appropriate for the coding problem: lookup and renewal are O(1), while counting is O(n), which is acceptable under the small constraints. In production, I would move token state to a shared store such as Redis so it survives application-instance restarts, is visible to every service instance, and benefits from native TTL expiration. I would also make renewal and generation atomic because `get -> check -> put` is a compound operation that can race under concurrency; simply switching to `ConcurrentHashMap` is not enough. Finally, I would avoid full O(n) counts at scale, use server-controlled time with `Clock`/`Instant`, generate cryptographically secure tokens, and enforce an absolute session lifetime where required.
