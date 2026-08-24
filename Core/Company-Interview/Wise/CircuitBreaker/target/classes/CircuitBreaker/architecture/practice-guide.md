# Circuit Breaker Interview Practice — Phase 1-3

> A source-informed practice reconstruction for a Java `WebClient` circuit-breaker exercise.
>
> **Important:** this is not claimed to be Wise's proprietary starter code. Public interview reports support the core shape of the exercise: a simulated remote call is supplied, and the candidate implements the `execute(...)` circuit-breaker logic. The exact boilerplate is not publicly established, so this pack deliberately separates **reported requirements** from **our practice extensions**.

---

## How to Use This Pack

Work through the phases in order.

1. Start only with the boilerplate in **Starter Code**.
2. Complete the original **Phase 1** rolling-window breaker.
3. Explain your design using the Phase 1 interview questions.
4. Complete both **Phase 1 extra variants**:
   - 3 consecutive failures within the rolling window;
   - 3 consecutive failures with no rolling-window constraint.
5. Treat **Phase 2** as a new interviewer concurrency change request.
6. Treat **Phase 3** as another new interviewer caching change request.
7. Complete the **Required Testing Challenge**.
8. Finish with the **Senior-Level Follow-Up Q&A**.

The goal is not only to make the code work. You should be able to explain:

- what state you introduced;
- why you chose each data structure;
- which operations must be atomic;
- which operations must remain concurrent;
- the exact time-boundary behaviour;
- the trade-offs you would make differently in production.

---

# Part I — Starter Exercise

## 1. Interview Scenario

Service A uses a `WebClient` to call downstream services such as Service B and Service C.

Implement circuit-breaker behaviour independently for each downstream service.

For the practice implementation:

| Setting                     |                  Value |
| --------------------------- | ---------------------: |
| Failure threshold           |             3 failures |
| Rolling failure window      |             20 seconds |
| Cooldown                    |             10 seconds |
| Successful response         |                  `200` |
| Qualifying failure response |                  `500` |
| Circuit isolation           | Per downstream service |

A service should stop receiving new downstream calls after three qualifying failures occur inside the active rolling window.

After the cooldown expires, the client should be able to test whether the service has recovered.

---

## 2. What the Interviewer Gives You

Treat the following as supplied boilerplate:

- `Request`
- `Response`
- `WebClient` skeleton
- failure threshold
- rolling failure window
- cooldown
- simulated downstream `call()`
- minimal `main()` that sends requests to Service B and Service C
- empty/basic `execute(Request request)`

### Deliberately **not** supplied

You must decide whether/how to introduce:

- per-service breaker state
- the failure-history data structure
- a map of service circuits
- blocked/open-state representation
- recovery-state representation
- behaviour when a call is blocked
- `CircuitOpenException`
- `Clock` or another time abstraction
- `HashMap`
- `ConcurrentHashMap`
- `ServiceCircuit`
- `CLOSED / OPEN / HALF_OPEN`
- synchronization or locks
- cache structures
- cache keys
- cache expiry

Do not assume these are part of the original boilerplate merely because they appear in a completed solution.

---

## 3. Starter Code

### `Request.java`

```java
package CircuitBreaker;

/**
 * Simple request object.
 */
public class Request {

    private final String service;

    public Request(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
```

### `Response.java`

```java
package CircuitBreaker;

/**
 * Simple response object.
 */
public class Response {

    int status;
    String body = "body";

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
```

### `WebClient.java`

```java
package CircuitBreaker;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class WebClient {

    private static final int FAILURE_THRESHOLD = 3;
    private static final Duration FAILURE_WINDOW = Duration.ofSeconds(20);
    private static final Duration COOLDOWN = Duration.ofSeconds(10);

    public Response execute(Request request) {
        // TODO: implement
        return null;
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

    /**
     * Demo harness only.
     */
    public static void main(String[] args) throws InterruptedException {

        WebClient webClient = new WebClient();
        Random random = new Random();

        for (int i = 0; i < 30; i++) {

            String service =
                    random.nextInt(2) == 0 ? "ServiceB" : "ServiceC";

            Request request = new Request(service);
            Response response = webClient.execute(request);

            System.out.println(
                    Instant.now() + " -> " + service + " : " + response
            );

            Thread.sleep(2000);
        }
    }
}
```

---

## 4. Exercise Rules

- Treat `call()` as supplied remote-call behaviour.
- Keep circuit-breaker logic around `call()`, not inside it.
- For the core phases, assume the supplied simulation returns `200` or `500`.
- Do not assume a blocked request should return `null`, `503`, or throw an exception unless the platform contract says so.
- In a real HackerRank exercise, the supplied method contract, samples and tests take precedence over this practice pack.
- Phase 1 assumes single-threaded execution.
- Complete both Phase 1 variants after the original Phase 1 exercise.
- Phase 2 introduces concurrency.
- Phase 3 introduces caching.
- Complete the Required Testing Challenge before the final revision/senior sections.
- `Thread.sleep()` in `main()` is only a demonstration aid; it is not a concurrency test.

---

# Part II — Phase 1: Basic Circuit Breaker

## 5. Phase 1 Requirement

Implement `WebClient.execute(Request request)`.

For each downstream service:

- `200` is a success.
- `500` is a qualifying failure.
- Open/block the service after **three qualifying failures inside a rolling 20-second window**.
- Failures do **not** need to be consecutive.
- A normal successful `200` does **not** clear active failure history.
- Circuit state must be isolated per service.
- While the service is blocked and the cooldown is active, **do not call `call()`**.
- The cooldown lasts 10 seconds.
- At the exact cooldown boundary, a recovery attempt may proceed.
- A successful recovery resets the breaker.
- A failed recovery blocks the service again immediately and restarts the cooldown.
- The request that produces failure #3 still receives its real downstream `500`; blocking affects subsequent requests.

### Rolling-window boundary

The active failure interval is:

```text
(now - 20 seconds, now]
```

Therefore, a failure exactly 20 seconds old is expired.

### Cooldown boundary

If:

```text
now < blockedAt + 10 seconds
```

the service remains blocked.

At exactly:

```text
now == blockedAt + 10 seconds
```

recovery is allowed.

---

## 6. Phase 1 TODOs

- [ ] Validate the request/service as appropriate.
- [ ] Decide how to maintain independent breaker state for each downstream service.
- [ ] Decide how to store failure timestamps.
- [ ] Remove failures that have left the rolling 20-second window.
- [ ] Determine when the threshold of three active failures has been reached.
- [ ] Prevent `call()` while the circuit is inside its cooldown.
- [ ] Allow a request after cooldown to test recovery.
- [ ] Reset correctly after successful recovery.
- [ ] Re-block immediately after failed recovery.
- [ ] Timestamp a failure at the time its downstream result is observed.
- [ ] Handle the exact rolling-window and cooldown boundaries.
- [ ] Decide how `execute()` communicates a circuit-open rejection to its caller.

---

## 7. Phase 1 Behaviour Checks

### Three non-consecutive failures can open the circuit

```text
500
200
500
200
500
```

If all three `500`s are still inside the rolling 20-second window:

```text
circuit opens
```

A normal `200` does not reset the failure history.

### An expired failure no longer counts

```text
failure #1
    |
    | more than 20 seconds
    v
failure #2
failure #3
```

Failure #1 is no longer active, so the circuit has only two active failures.

### The threshold-producing request is not retroactively rejected

```text
request
  |
  v
downstream returns 500  <-- failure #3
  |
  v
record failure
  |
  v
open circuit
  |
  v
return the real 500
```

The **next** request is the one that fails fast.

### Blocked call

```text
request
  |
  v
service still inside cooldown
  |
  v
DO NOT call downstream
```

The original public requirement does not establish the exact blocked-return contract. In a real HackerRank exercise, inspect the supplied contract before inventing one.

---

## 8. Phase 1 Interview Questions

1. Why is a deque a good choice for failure timestamps in the single-threaded version?
2. Why maintain one circuit per downstream service?
3. Do the three failures need to be consecutive?
4. Should a normal `200` clear previous failures?
5. What happens to a failure exactly 20 seconds old?
6. When should the cooldown begin?
7. What should happen to the request that creates failure #3?
8. Why should the failure timestamp be captured after `call()`?
9. How would you test time-dependent behaviour without relying on `Thread.sleep()`?
10. Is this Phase 1 implementation safe under concurrent traffic?
11. What should `execute()` do when the circuit rejects a request?

### Phase 1 — Concise Answer Key

### 1. Why a deque?

A rolling window needs frequent append of new failures and removal of expired failures. In single-threaded execution, timestamps remain ordered, so a deque supports efficient append and front removal.

### 2. Why one circuit per service?

Failure isolation. Service B being unhealthy must not block healthy Service C.

### 3. Do failures need to be consecutive?

No. The rule is three failures **within 20 seconds**, not three consecutive failures.

### 4. Should a normal `200` clear previous failures?

No. Doing so would change the policy into "three consecutive failures."

### 5. What happens exactly at 20 seconds?

The failure expires. The active window is `(now - 20s, now]`.

### 6. When does cooldown begin?

When the qualifying failure that opens the circuit is observed.

### 7. What does failure #3 receive?

Its real downstream `500`. Opening the circuit affects subsequent requests.

### 8. Why timestamp after `call()`?

The downstream call may take time. Recording the earlier admission time would distort the rolling failure window.

### 9. How should time be tested?

Abstract the source of "now." An injected Java `Clock` is a clean option.

`Clock` is a solution/testing improvement, **not assumed starter boilerplate**.

### 10. Is Phase 1 thread-safe?

No. Shared map/state/failure-history operations can race under concurrent access.

### 11. What should happen on circuit rejection?

First follow the actual API contract. If the contract is silent, a custom `CircuitOpenException` is a clean practice choice because `null` is ambiguous and can cause a later `NullPointerException`. Do not assume HackerRank expects that custom exception unless its contract supports it.

---

# Phase 1 Extra Practice — Consecutive Failure Variant

## 9. Variant Requirement

Keep the Phase 1 circuit breaker, but change one failure rule:

> Open the circuit when the **third consecutive qualifying `500`** occurs and the three failures are all inside the active rolling 20-second window.

A normal successful `200` now **breaks the failure streak** and clears the current consecutive-failure history.

All other Phase 1 behaviour remains unchanged:

- breaker state is isolated per downstream service;
- the active rolling window remains `(now - 20 seconds, now]`;
- a failure exactly 20 seconds old is expired;
- the cooldown remains 10 seconds;
- the request producing failure #3 receives its real downstream `500`;
- subsequent requests fail fast during cooldown;
- successful recovery resets the breaker;
- failed recovery immediately blocks the service again and restarts cooldown.

### Meaning of "3 consecutive failures"

For this variant, interpret the requirement as:

```text
open when the third consecutive qualifying failure occurs
```

not as a separate "exactly three and never more" state. Once the third qualifying failure opens the circuit, subsequent normal downstream calls should no longer be admitted during cooldown.

---

### Behaviour comparison

#### Original Phase 1 rule — any 3 failures in the rolling window

```text
500
200
500
200
500
```

If all three `500`s are still inside 20 seconds:

```text
OPEN
```

#### Consecutive-failure variant

```text
500   -> streak = 1
200   -> streak reset
500   -> streak = 1
200   -> streak reset
500   -> streak = 1
```

Result:

```text
CLOSED
```

#### Three consecutive failures inside the window

```text
500
500
500
```

If all three are inside the rolling 20-second window:

```text
OPEN
```

#### Consecutive failures that do not all fit inside the window

```text
t=00s  500
t=11s  500
t=21s  500
```

At `t=21s`, the first failure is outside the 20-second window, so only two active failures remain:

```text
CLOSED
```

If another `500` arrives at `t=22s`:

```text
t=11s  500
t=21s  500
t=22s  500
```

those three consecutive failures fit inside the active window, so the circuit opens.

---

### Variant TODOs

- [ ] Preserve independent breaker state per downstream service.
- [ ] Track the current consecutive sequence of qualifying failures.
- [ ] Clear that sequence when a normal `200` is observed.
- [ ] Continue expiring failures that fall outside the rolling 20-second window.
- [ ] Open the circuit when the third active consecutive failure is recorded.
- [ ] Preserve the exact rolling-window boundary.
- [ ] Preserve the Phase 1 cooldown and recovery behaviour.
- [ ] Preserve the rule that failure #3 receives its real downstream `500`.
- [ ] Explain whether your original failure data structure still fits this variant.

---

### Variant Interview Questions

1. What is the key difference from the original Phase 1 rule?
2. What does a normal `200` do to failure history now?
3. Do you still need the rolling-time window?
4. Is a simple integer consecutive-failure counter sufficient by itself?
5. Can the original deque-based design still work?
6. What should happen when old failures expire but no success has occurred?
7. Does the cooldown/recovery model need to change?
8. Why is this a useful interview variant?

### Consecutive Failure Variant — Concise Answer Key

### 1. What changed?

The original rule counts any three failures in the window. This variant requires the qualifying failures to be uninterrupted by a successful `200`.

### 2. What does a normal `200` do now?

It resets the current consecutive-failure sequence.

### 3. Is the rolling window still needed?

Yes. The three consecutive failures must also fit inside the configured 20-second window.

### 4. Is an integer counter alone enough?

Not if the rolling-window requirement remains. You also need timing information so old failures can stop counting.

### 5. Can the deque still work?

Yes. Keep timestamps for the current consecutive failure sequence, clear them on `200`, prune expired timestamps, append each new `500`, and open when three active timestamps remain.

### 6. What if an old failure expires without a success?

Remove the expired timestamp but keep newer failures in the current streak. A later failure can still complete a new group of three consecutive failures inside the active window.

### 7. Does recovery change?

No. The 10-second cooldown and successful/failed recovery semantics can remain the same as Phase 1.

### 8. Why is this useful practice?

It shows that a small wording change can change state-management semantics. In the original rule, normal success preserves failure history; in this variant, normal success must clear it.

---

# Phase 1 Extra Practice — Pure Consecutive Failure Variant

## 10. Pure Consecutive Variant Requirement

Now remove the rolling-failure-window requirement entirely.

The circuit should open when a downstream service returns **3 consecutive qualifying `500` responses**, regardless of how much wall-clock time passes between those failures.

A normal successful `200` breaks the streak and resets the consecutive-failure count.

Preserve the other Phase 1 guarantees:

- state remains isolated per downstream service;
- the request producing failure #3 still receives its real downstream `500`;
- subsequent requests fail fast during the 10-second cooldown;
- a successful recovery resets the breaker;
- a failed recovery reopens immediately and restarts the cooldown.

### The key semantic difference

This sequence opens the circuit:

```text
500 at 09:00
500 at 10:00
500 at 15:00

-> OPEN
```

There is no rolling window anymore, so elapsed time between failures does not matter as long as no success breaks the streak.

This does **not** open the circuit:

```text
500
500
200
500
500

-> CLOSED
```

The `200` resets the consecutive-failure streak.

### Simplified state implication

For the failure threshold itself, you no longer need failure timestamps.

Conceptually, the state can be reduced to:

```text
consecutiveFailures
blocked/open state
cooldown timestamp
```

You still need time for the **cooldown**, but not for deciding whether the three failures qualify.

### Phase 1 rule comparison

| Variant                      | Opens on                                   | Does normal `200` reset failure progress? | Failure timestamps needed for threshold? |
| ---------------------------- | ------------------------------------------ | ----------------------------------------: | ---------------------------------------: |
| Original Phase 1             | Any 3 failures inside 20s                  |                                        No |                                      Yes |
| Consecutive + rolling window | 3 consecutive failures that fit inside 20s |                                       Yes |                                      Yes |
| Pure consecutive             | 3 consecutive failures, no time window     |                                       Yes |                                       No |

### Pure Consecutive Variant TODOs

- [ ] Replace rolling-window failure counting with consecutive-failure tracking.
- [ ] Reset the failure streak on a normal successful `200`.
- [ ] Open the circuit on the third consecutive qualifying failure.
- [ ] Keep breaker state isolated per service.
- [ ] Preserve the 10-second cooldown.
- [ ] Preserve successful- and failed-recovery behaviour.
- [ ] Ensure the third failing request still receives its real downstream response.
- [ ] Remove failure-window state that is no longer required.
- [ ] Explain why time is still required for cooldown even though it is no longer required for threshold counting.

### Pure Consecutive Variant Interview Questions

1. What changed compared with the original Phase 1 rule?
2. What changed compared with the consecutive-plus-rolling-window variant?
3. What is the simplest failure-tracking state now?
4. Do you still need a deque of failure timestamps?
5. Can three failures several hours apart open the circuit?
6. What does a normal `200` do?
7. Is a clock/time source still needed anywhere?
8. What happens when the recovery probe returns `500`?

### Pure Consecutive Variant — Concise Answer Key

### 1. What changed from original Phase 1?

The breaker now cares about an uninterrupted failure streak, not any failures accumulated inside a time window.

### 2. What changed from consecutive + rolling window?

The rolling-window condition is gone. Only consecutiveness matters.

### 3. Simplest failure-tracking state?

A per-service consecutive-failure counter is enough for threshold tracking.

### 4. Do you still need a failure timestamp deque?

No, not for the failure threshold. There is no rolling failure window to prune.

### 5. Can failures hours apart open the circuit?

Yes, if no successful response occurs between them. That is the consequence of removing the time-window constraint.

### 6. What does a normal `200` do?

It resets the consecutive-failure count to zero.

### 7. Is time still needed?

Yes, for the 10-second cooldown after the circuit opens. It is no longer needed to decide whether failures qualify for the threshold.

### 8. What if recovery returns `500`?

Reopen immediately and restart the cooldown. The service just proved it is still unhealthy; it does not need to build a new three-failure streak first.

---

# Part III — Phase 2: Make the Breaker Concurrency-Safe

> **New interviewer change request:** the Phase 1 behaviour is correct, but `WebClient` is now shared by many application threads.

## 11. Phase 2 Concurrency Scenario

Service B is already blocked.

The 10-second cooldown finishes.

Then:

```text
20 requests arrive at the same time
```

Required result:

```text
exactly 1 request -> allowed to test recovery

other 19 requests -> rejected / fail fast
```

The single recovery request must make its downstream call **without holding a circuit lock**.

---

## 12. Phase 2 Requirements

Preserve all Phase 1 semantics, plus:

- `execute()` may be called concurrently.
- Per-service circuit lookup/creation must be thread-safe.
- State changes inside a service circuit must be race-free.
- Exactly one request may test recovery after cooldown.
- Competing requests must fail fast while that recovery request is in flight.
- Requests to different downstream services should not contend on one global circuit lock.
- Normal downstream calls should not be unnecessarily serialized.
- Never hold the circuit-state lock across network I/O.
- Existing calls already in flight are not cancelled merely because another request opens the circuit.
- Concurrent downstream calls may finish in a different order from the order in which they started.

---

## 13. Phase 2 TODOs

- [ ] Identify every piece of shared mutable state from Phase 1.
- [ ] Make per-service circuit lookup/creation safe under concurrency.
- [ ] Decide how to represent "one recovery request is already in flight."
- [ ] Make the recovery-admission decision atomic.
- [ ] Guarantee exactly one recovery probe after cooldown.
- [ ] Reject competing callers while recovery is being tested.
- [ ] Make failure pruning, recording and state transitions atomic.
- [ ] Keep the downstream network call outside the circuit lock.
- [ ] Avoid a single global lock across all services.
- [ ] Revisit any Phase 1 algorithm that depended on timestamp insertion order.
- [ ] Preserve exact time-boundary semantics.

---

## 14. Phase 2 Acceptance Checks

### Exactly-one recovery

```text
OPEN
 |
 | cooldown expires
 v
20 concurrent callers
 |
 +--> exactly one downstream recovery call
 |
 +--> all competing callers fail fast
```

### Healthy traffic remains concurrent

If the circuit is operating normally:

```text
Thread A ---- call() ---------------->

Thread B ---- call() ---------------->

Thread C ---- call() ---------------->
```

Normal network calls should not be serialized behind one long-held circuit lock.

### Per-service isolation remains

```text
Service B circuit lock != Service C circuit lock
```

Heavy state activity for Service B should not unnecessarily block Service C's circuit operations.

---

## 15. Phase 2 Interview Questions

1. Why is `ConcurrentHashMap` alone not enough?
2. What race occurs when multiple threads observe an expired cooldown?
3. Why introduce an explicit `HALF_OPEN` state?
4. Which admission operations must be atomic?
5. Which failure-handling operations must be atomic?
6. Why synchronize per `ServiceCircuit` instead of globally?
7. Why not synchronize the entire `execute()` method?
8. Why must the downstream `call()` happen outside the circuit lock?
9. What happens to calls already in flight when another request opens the circuit?
10. Could `AtomicInteger` replace synchronization?
11. Could `ReentrantLock` be used instead of `synchronized`?
12. When would `ReentrantLock` be preferable?
13. Why can Phase 1's ordered-deque cleanup become invalid under concurrency?
14. How should expired failures be removed if completion order is not chronological?
15. Does per-circuit synchronization serialize all downstream requests?

### Phase 2 — Concise Answer Key

### 1. Why isn't `ConcurrentHashMap` enough?

It makes map operations thread-safe. It does not make compound state transitions inside each `ServiceCircuit` atomic.

### 2. What cooldown race exists?

Several threads could all observe `OPEN + cooldown expired` and all become recovery probes.

### 3. Why `HALF_OPEN`?

It explicitly represents that one recovery probe is already in flight, allowing competing callers to be rejected.

### 4. What admission work must be atomic?

At minimum:

```text
inspect state
check cooldown
OPEN -> HALF_OPEN
return the admission decision
```

### 5. What failure-handling work must be atomic?

At minimum:

```text
prune active failures
record the new failure
check threshold/state
possibly transition CLOSED -> OPEN
```

### 6. Why synchronize per circuit?

It confines contention to callers sharing one downstream service. Service B should not block Service C's circuit state changes.

### 7. Why not synchronize all of `execute()`?

That would serialize network calls and reduce throughput unnecessarily.

### 8. Why call downstream outside the lock?

Network I/O can be slow or hang. Holding the lock across it would make other callers wait for the remote operation.

### 9. What about already in-flight calls?

They continue. Opening the circuit prevents new admissions; it does not cancel calls already executing.

### 10. Why not just `AtomicInteger`?

The invariant spans multiple values: failure history, circuit state and timestamps. Making one counter atomic does not make the overall transition atomic.

### 11. Could `ReentrantLock` work?

Yes. It can provide the same mutual exclusion.

### 12. When prefer `ReentrantLock`?

When you need features such as `tryLock`, timed lock acquisition, interruptible acquisition or `Condition`s.

### 13. Why does ordered front-pruning become unsafe?

Concurrent requests may complete out of order, so insertion order no longer guarantees chronological timestamp order.

### 14. How should cleanup work then?

Inspect the retained timestamps and remove every expired one instead of assuming the oldest timestamp is always at the front.

### 15. Does synchronization serialize all downstream requests?

No. Only the short circuit-state critical sections are serialized. The remote calls remain outside the lock and can run concurrently.

---

# Part IV — Phase 3: Add a 30-Second Response Cache

> **New practice extension:** repeated successful requests are generating unnecessary downstream traffic. Add a response cache while preserving every Phase 2 concurrency guarantee.

Phase 3 is an intentional extension of the base circuit-breaker exercise; do not treat it as confirmed original Wise boilerplate.

## 16. Phase 3 API Change

A cache needs request identity in addition to the service name.

For this practice stage, assume the execution API can identify:

```text
service + requestKey
```

For example:

```java
execute("ServiceB", "account-123")
```

You may model this with a richer `Request` object or an overloaded/new `execute(...)` signature.

The important requirement is the cache identity, not the exact Java surface API.

---

## 17. Phase 3 Requirements

- Cache successful `200` responses.
- Cache TTL is 30 seconds.
- Never cache `500` responses.
- Scope each cache entry by:

```text
(service, requestKey)
```

- Check the cache **before** circuit-breaker admission.
- A fresh cache hit returns immediately.
- A fresh cache hit does not call the downstream service.
- A fresh cache hit does not touch/update breaker state.
- A fresh cached response may therefore be returned while the downstream circuit is `OPEN`.
- An expired cache entry must not be served.
- At exactly `expiresAt`, the entry is expired.
- Cache access/removal must be safe under concurrency.
- On cache miss/expiry, preserve all Phase 2 circuit-breaker behaviour.

### TTL boundary

```text
now < expiresAt      -> fresh
now >= expiresAt     -> expired
```

---

## 18. Phase 3 TODOs

- [ ] Decide how cache entries are uniquely identified.
- [ ] Store enough metadata to determine cache expiry.
- [ ] Place cache lookup before circuit-breaker admission.
- [ ] Return fresh cached responses immediately.
- [ ] Ensure expired values are never served.
- [ ] Remove an expired value without accidentally deleting a newer concurrent replacement.
- [ ] Cache only successful downstream responses.
- [ ] Keep cache operations concurrency-safe.
- [ ] Preserve Phase 2 exactly-one recovery semantics on cache misses.
- [ ] Consider the memory behaviour of the cache.
- [ ] Consider whether returning the same mutable `Response` instance is safe.

---

## 19. Phase 3 Behaviour Checks

### Fresh cache hit

```text
request
  |
  v
cache lookup
  |
  v
fresh hit
  |
  v
return cached response

NO circuit admission
NO downstream call
```

### Cache miss while circuit is open

```text
request
  |
  v
cache miss
  |
  v
circuit admission
  |
  v
OPEN / not recoverable yet
  |
  v
fail fast
```

### Fresh cache hit while circuit is open

```text
request
  |
  v
fresh cache hit
  |
  v
return response
```

This is valid because no downstream call is being attempted. The breaker controls whether the client may contact the unhealthy dependency; it does not prohibit returning already-available local data.

### Successful downstream call

```text
cache miss
   |
   v
breaker admits request
   |
   v
downstream returns 200
   |
   +--> update breaker outcome
   |
   +--> cache response for 30 seconds
```

### Failed downstream call

```text
downstream returns 500
   |
   +--> update breaker outcome
   |
   +--> DO NOT cache
```

---

## 20. Phase 3 Interview Questions

1. Why check the cache before the circuit breaker?
2. Can a fresh cached response be returned while the circuit is `OPEN`?
3. Should a cache hit update circuit-breaker health?
4. Why should the cache key contain both `service` and `requestKey`?
5. Why should `CacheKey` be immutable?
6. Why must `CacheKey.equals()` and `hashCode()` use the same identity fields?
7. Why cache only successful `200` responses?
8. What happens exactly at the 30-second TTL boundary?
9. Why base cache expiry on the time the successful outcome was observed rather than when the request started?
10. Why must cache access be thread-safe?
11. What does `ConcurrentHashMap` make safe, and what does it *not* make atomic?
12. Why can `remove(key, oldValue)` be safer than plain `remove(key)`?
13. If two threads miss the same cache key at the same time, can both call downstream?
14. Does this implementation prevent a cache stampede?
15. How could you prevent or reduce duplicate downstream loads for the same key?
16. What happens if two concurrent successful loads write the same cache key?
17. What is the risk of caching and returning the same mutable `Response` object?
18. Does the cache replace the circuit breaker?
19. What is the main trade-off when serving cached data?
20. What is the risk of an unbounded `ConcurrentHashMap` cache?
21. What is lazy expiry, and what are its main advantage and disadvantage?
22. How would you improve this cache in production?

### Phase 3 — Concise Answer Key

### 1. Why cache before the breaker?

A fresh cache hit can satisfy the request without contacting the downstream service, so circuit admission is unnecessary. This reduces downstream load and improves availability during dependency failures.

### 2. Can a fresh cache serve while the circuit is `OPEN`?

Yes. The breaker controls whether a new downstream call may be attempted. Returning already-available fresh local data does not contact the unhealthy dependency.

### 3. Should a cache hit update breaker health?

No. A cache hit gives no new evidence about the current health of the downstream service, so it should neither record success nor failure.

### 4. Why `(service, requestKey)`?

The same request identity may exist for different downstream services. Including both fields prevents unrelated services or requests from incorrectly sharing one cached response.

### 5. Why should `CacheKey` be immutable?

Hash-map keys must remain stable after insertion. If a field used by `equals()` or `hashCode()` changes, the map may no longer be able to locate the entry correctly.

### 6. Why must `equals()` and `hashCode()` use the same fields?

Hash-based maps require equal objects to have equal hash codes. If equality uses `(service, requestKey)`, the hash code must be derived from the same logical identity.

### 7. Why cache only `200`?

Caching a temporary failure could keep serving that error after the downstream service has recovered. In this exercise, only successful responses become cache entries.

### 8. What happens exactly at TTL?

The value is expired. Freshness is:

```text
now < expiresAt
```

Therefore:

```text
now >= expiresAt
```

means expired.

### 9. Why calculate expiry from outcome time?

A downstream call may be slow. Starting the TTL when the request began would shorten the usable lifetime of the cached response. Using the observed success time means the entry receives the intended full TTL after the result is actually available.

### 10. Why thread-safe cache access?

Multiple request threads may read, insert, replace and remove entries simultaneously. The cache container must support those operations safely under concurrency.

### 11. What does `ConcurrentHashMap` guarantee, and what does it not?

It makes individual map operations such as `get`, `put` and conditional `remove` thread-safe. It does **not** make the whole sequence:

```text
check cache
-> miss
-> call downstream
-> populate cache
```

one atomic operation.

### 12. Why `remove(key, oldValue)`?

Another thread may replace the expired value with a fresh entry after the current thread reads the old one. Conditional removal deletes the entry only if the map still contains the exact stale value that was observed, so it does not accidentally remove the newer replacement.

### 13. Can two threads miss the same key and both call downstream?

Yes. Both can observe the cache miss before either has populated the cache, and both can then pass through circuit admission and make downstream calls.

### 14. Does this implementation prevent cache stampede?

No. The cache is thread-safe, but same-key cache misses are not coordinated. Many callers can therefore load the same missing or expired key concurrently.

### 15. How could duplicate loads be reduced?

Use request coalescing / single-flight semantics, a per-key loading lock, a shared `CompletableFuture`, or a cache library that guarantees coordinated per-key loading.

The goal is:

```text
many callers miss same key
        |
        v
one caller loads downstream
        |
        v
other callers share/wait for that result
```

rather than every caller performing the same downstream load.

### 16. What if two concurrent successful loads write the same key?

`ConcurrentHashMap.put()` keeps the map structurally safe, and the later write wins. For equivalent read-only results this may be acceptable, but it still represents duplicate downstream work and can matter if responses differ between the two calls.

### 17. Why is a mutable cached `Response` risky?

The cache stores the same object reference. If one caller mutates that `Response`, another caller may later observe the modified object. Prefer immutable cached values or defensive copies when mutation is possible.

### 18. Does caching replace the breaker?

No. Cache misses and expired entries still require circuit admission before contacting downstream.

### 19. Main caching trade-off?

Freshness versus availability and performance.

A longer TTL reduces downstream traffic and increases the chance that requests can be served during an outage, but it also increases the chance of returning older data.

### 20. Why is an unbounded map risky?

One-off request keys can accumulate indefinitely and consume memory. `ConcurrentHashMap` provides concurrency safety, not eviction or capacity management.

### 21. What is lazy expiry?

Lazy expiry means an expired entry is discovered and removed when that key is accessed rather than by a background cleanup task.

Its advantage is simplicity and no background sweeper.

Its disadvantage is that expired entries whose keys are never accessed again may remain in memory.

### 22. How would you improve the cache in production?

Use a bounded cache with explicit TTL and eviction behaviour, preferably a mature cache library. Depending on requirements, also consider:

- maximum size / eviction policy;
- request coalescing for same-key misses;
- immutable cached values;
- metrics for hit rate, miss rate, eviction and load latency;
- service-specific TTLs;
- carefully defined cacheability rules.
---

# Part V — Required Testing Challenge

## 21. Required Testing Challenge

Do not consider the practice pack complete until you can verify the behaviour with deterministic tests.

The purpose is to test **state transitions and boundaries**, not to wait for real time or depend on random downstream results.

### Testing constraints

- Do not use `Thread.sleep()` to prove time-boundary behaviour.
- Do not rely on the random supplied `call()` when asserting exact outcomes.
- Use a controllable source of time in tests.
- Use a controllable downstream stub/fake so you can force `200`, `500`, blocking calls and call counts.
- Verify both returned behaviour **and** whether the downstream function was invoked.
- For concurrency tests, coordinate thread start with latches/barriers rather than hoping threads overlap.

If your implementation keeps `call()` overridable, a test subclass can provide deterministic responses. If you refactor the practice code, an injected downstream function/client is also reasonable. The real HackerRank contract still takes precedence over any refactoring.

---

### A. Original Phase 1 — Required Tests

| Test                                  | Expected result                  |
| ------------------------------------- | -------------------------------- |
| Two active failures                   | Circuit remains closed           |
| `500, 200, 500, 200, 500` inside 20s  | Circuit opens                    |
| Oldest failure exactly 20s old        | That failure is expired          |
| Failure #3 occurs                     | That request gets its real `500` |
| Next request during cooldown          | Downstream is not called         |
| Request just before cooldown boundary | Rejected                         |
| Request exactly at cooldown boundary  | Recovery is allowed              |
| Recovery returns `200`                | Breaker resets                   |
| Recovery returns `500`                | Breaker reopens immediately      |
| Service B opens                       | Service C remains unaffected     |

#### Phase 1 testing question

**What are you really testing at the 20-second boundary?**

That the active interval is `(now - 20s, now]`: equality at the lower boundary is expired.

---

### B. Consecutive + Rolling-Window Variant — Required Tests

| Sequence                                         | Expected result                                                   |
| ------------------------------------------------ | ----------------------------------------------------------------- |
| `500, 500, 500` inside 20s                       | Open                                                              |
| `500, 500, 200, 500, 500`                        | Closed                                                            |
| `500@t0, 500@t19, 500@t21`                       | Closed                                                            |
| Then another `500@t22` with no intervening `200` | Open if the last three active consecutive failures fit inside 20s |
| Normal `200`                                     | Consecutive sequence resets                                       |

#### Why `t0, t19, t21` stays closed

At `t21`, the failure at `t0` is outside the active 20-second window. Only two qualifying failures remain in the active consecutive sequence.

---

### C. Pure Consecutive Variant — Required Tests

| Sequence                                        | Expected result                                       |
| ----------------------------------------------- | ----------------------------------------------------- |
| `500, 500, 500`                                 | Open                                                  |
| `500, 500, 200, 500, 500`                       | Closed                                                |
| `500@09:00, 500@10:00, 500@15:00` with no `200` | Open                                                  |
| Normal `200` after two failures                 | Counter resets to zero                                |
| Failed recovery                                 | Reopen immediately; do not require three new failures |

This test set proves that the pure-consecutive version has **no failure-window semantics**.

---

### D. Phase 2 Concurrency — Required Tests

#### Exactly-one recovery probe

Set one service to `OPEN`, advance time to the exact cooldown boundary, then release many threads together.

Expected:

```text
1 caller  -> reaches downstream recovery call
N-1       -> rejected / fail fast
```

Assert the downstream recovery call count is exactly `1`.

#### Lock is not held across normal network I/O

Use a downstream stub that blocks until released.

Start multiple normally admitted calls to the same healthy service.

Expected:

```text
more than one call can be in flight concurrently
```

If only one can enter the downstream stub at a time, you probably held the circuit lock across `call()`.

#### Per-service isolation

Block or heavily contend Service B's circuit-state operation while calling Service C.

Service C should still make progress independently.

#### In-flight request behaviour

Admit multiple `CLOSED` requests, block them inside the downstream stub, then let one complete with the threshold-producing failure.

Expected:

- circuit becomes `OPEN`;
- already-admitted calls are not cancelled;
- their results are still processed when they finish.

---

### E. Phase 3 Cache — Required Tests

| Test                                                 | Expected result                                  |
| ---------------------------------------------------- | ------------------------------------------------ |
| First successful request                             | Downstream called and result cached              |
| Same `(service, requestKey)` before TTL              | Cache hit; downstream not called again           |
| Fresh cache hit while circuit is `OPEN`              | Cached response returned                         |
| Cache miss while circuit is `OPEN`                   | Fail fast; downstream not called                 |
| Entry exactly at `expiresAt`                         | Expired                                          |
| `500` response                                       | Never cached                                     |
| Same request key, different service                  | Different cache entries                          |
| Different request keys, same service                 | Different cache entries                          |
| Expired value replaced concurrently by a fresh value | Expiry cleanup must not remove fresh replacement |

#### Conditional-removal test

A useful race to model is:

```text
Thread A reads expired value V1
Thread B writes fresh value V2
Thread A performs expiry cleanup
```

Correct result:

```text
V2 remains in cache
```

This is what a compare-and-remove operation such as `remove(key, V1)` protects.

---

### F. Testability Interview Q&A

**Why is `Thread.sleep()` a poor unit-test strategy?**
It makes tests slow, timing-dependent and flaky.

**Why use a controllable clock/time source?**
It lets the test move directly to exact boundaries such as 19.999s, 20s and 10s without waiting.

**Why use a deterministic downstream stub?**
You need to force exact success/failure sequences and count whether downstream was called.

**What should a circuit-open test assert besides the return/exception?**
That the downstream call count did not increase.

**Why coordinate concurrent threads with a barrier/latch?**
It creates the race intentionally instead of depending on scheduler luck.

**What is the most important Phase 2 concurrency assertion?**
Exactly one caller becomes the recovery probe.

**What is the most important Phase 3 cache assertion?**
A fresh hit returns without touching either the breaker admission path or downstream service.

**What should boundary tests avoid?**
Approximate timing. Test the exact boundary semantics defined by the requirement.

---

# Part VI — Phase Evolution Cheat Sheet

## 22. Phase 1 → Phase 3

| Concern                           | Phase 1 | Phase 2 | Phase 3 |
| --------------------------------- | ------- | ------- | ------- |
| Rolling-window breaker            | Yes     | Yes     | Yes     |
| Per-service state                 | Yes     | Yes     | Yes     |
| Three failures / 20s              | Yes     | Yes     | Yes     |
| 10s cooldown                      | Yes     | Yes     | Yes     |
| Thread-safe                       | No      | Yes     | Yes     |
| Explicit recovery-in-flight state | No      | Yes     | Yes     |
| Exactly one recovery probe        | No      | Yes     | Yes     |
| Response cache                    | No      | No      | Yes     |
| Cache before breaker              | No      | No      | Yes     |
| 30s cache TTL                     | No      | No      | Yes     |
| Network call under circuit lock   | No      | No      | No      |

---

# Part VII — Senior-Level Follow-Up Q&A

These questions test whether you understand the design beyond simply making the coding exercise pass.

## 23. Architecture

### Q1. Is this circuit breaker distributed?

No. Its state exists only inside one JVM/process.

### Q2. What happens if the application runs on 10 JVM instances?

Each instance maintains and trips its own local circuit independently.

### Q3. Is local breaker state necessarily wrong?

No. Local breakers are common and avoid a new coordination dependency, but they do not provide globally shared breaker state.

### Q4. Would you put circuit-breaker state in Redis by default?

Usually no. Distributed coordination adds latency, complexity and another dependency to a mechanism intended to protect you from dependency failures.

---

## 24. Production Implementation

### Q5. Would you normally build this circuit breaker yourself in production?

Usually not. Prefer a mature resilience library unless requirements genuinely justify custom behaviour.

### Q6. Why use a mature library?

It provides tested concurrency, state transitions, metrics, configuration and edge-case handling.

### Q7. What still belongs to your application team?

Failure classification, thresholds, timeouts, fallback behaviour, service-specific policy, observability and how resilience mechanisms are composed.

---

## 25. Failure Classification

### Q8. Should every non-`200` response count as a circuit-breaker failure?

No. Count failures that indicate downstream unavailability or instability according to your policy.

### Q9. Would a normal `400 Bad Request` usually count?

No. It usually represents a client/request problem rather than downstream instability.

### Q10. What failures commonly count?

Configured server errors, timeouts, connection failures and similar availability failures.

### Q11. Should transport exceptions count?

Often yes for relevant exceptions such as timeouts or connection failures, but this must be an explicit failure policy.

> **Practice-pack scope:** the core Phase 1–3 exercise assumes the supplied downstream simulation returns `200` or `500`. Transport-exception classification is treated as this senior-level extension.

---

## 26. Timeouts

### Q12. Why does a circuit breaker still need a timeout?

A breaker cannot protect latency effectively if an individual downstream call is allowed to hang indefinitely.

### Q13. What if the single recovery probe hangs?

The circuit may remain in its recovery-in-progress state until the call finishes unless the downstream operation has a timeout.

### Q14. Timeout vs circuit breaker?

A timeout limits how long **one call** waits. A circuit breaker stops **future calls** when repeated failures show a dependency is unhealthy.

---

## 27. Retry

### Q15. Retry vs circuit breaker?

Retry attempts an operation again. A circuit breaker stops attempts when repeated failures indicate the dependency is unhealthy.

### Q16. Can they be used together?

Yes, but retries must be bounded. Aggressive retries can amplify load during an outage.

### Q17. Which one should wrap the other?

There is no universal answer. Composition should match the intended failure accounting and retry policy; the key is to keep retries bounded and make breaker metrics reflect meaningful failures.

---

## 28. Concurrency and Locking

### Q18. Why lock per service circuit?

It limits contention to callers sharing the same downstream dependency.

### Q19. Why avoid one global lock?

A busy or unhealthy Service B should not serialize circuit-state work for Service C.

### Q20. What is the most important locking rule here?

Protect shared state transitions, but do **not** hold the circuit lock across remote/network I/O.

### Q21. Why isn't `AtomicInteger` enough?

The correctness invariant spans multiple pieces of state, not one atomic count.

### Q22. `synchronized` vs `ReentrantLock`?

Use `synchronized` for simple mutual exclusion. Consider `ReentrantLock` when you need capabilities such as `tryLock`, timed/interruptible acquisition or conditions.

### Q23. Does circuit opening cancel in-flight requests?

Not in this design. It rejects new admissions; existing calls finish and their outcomes are processed.

---

## 29. Observability

### Q24. What metrics would you expose?

At minimum:

- current circuit state
- state-transition count
- downstream successes
- qualifying failures
- circuit-open rejections
- recovery attempts
- downstream latency
- timeout count

### Q25. Why measure circuit-open rejections?

They show how much application traffic is being affected by the unhealthy dependency.

### Q26. Why measure state transitions?

Repeated `OPEN -> recovery -> OPEN` cycles can reveal a flapping or unstable dependency.

---

## 30. Configuration

### Q27. Should every downstream service use the same threshold?

Not necessarily. Dependencies can have different traffic, latency, criticality and failure characteristics.

### Q28. What might be configured per service?

- failure threshold
- failure window
- cooldown
- request timeout
- qualifying failures
- retry policy
- cache TTL

---

## 31. Caching

### Q29. What is the main caching trade-off?

Freshness versus availability and performance.

### Q30. When is caching a poor fit?

When data must be strongly current, cache identity cannot be made safe, or the operation has side effects.

### Q31. Would you normally response-cache a payment `POST`?

Not as ordinary read-response caching. Side-effecting operations need different correctness mechanisms such as idempotency and transactional guarantees.

### Q32. Would you use an unbounded `ConcurrentHashMap` as a production cache?

Usually not. Prefer bounded cache behaviour with explicit expiry/eviction.

### Q33. What is a cache stampede here?

Many callers miss/expire the same key and all call downstream simultaneously.

### Q34. Does the Phase 3 implementation prevent a cache stampede?

No. The cache is thread-safe, but it does not coordinate one loader per key.

### Q35. How could you reduce duplicate loads?

Use request coalescing/single-flight or a cache API that coordinates per-key loading.

### Q36. What is the mutable-response concern?

Caching and returning the same mutable `Response` instance can let one caller modify data observed by another. Immutable cached values or defensive copies are safer.

---

## 32. Testing

### Q37. What are the highest-value tests?

Boundary and state-transition tests:

- failure #3 opens the circuit
- normal `200` does not erase active failure history
- failure exactly 20 seconds old expires
- request immediately before cooldown boundary is rejected
- request exactly at cooldown boundary is eligible for recovery
- successful recovery resets
- failed recovery reopens immediately
- exactly one recovery probe under concurrency
- unrelated service circuits remain independent
- cache hit
- cache miss
- exact cache TTL expiry
- conditional expired-value removal
- no caching of `500`

### Q38. Why avoid `Thread.sleep()` in unit tests?

It makes tests slow, timing-dependent and flaky.

### Q39. How should time be tested?

Inject or otherwise abstract the source of current time so the test can advance time deterministically.

### Q40. How do you test exactly-one recovery admission?

Release many threads against the same service at the recovery boundary and verify only one reaches the downstream recovery call.

### Q41. How would you verify that the lock is not held across network I/O?

Use concurrent test calls with a controllable/blocking downstream stub and verify multiple normally admitted calls can be in flight at the same time.

---

# Part VIII — Final Revision Sheet

## 33. Phase 1

```text
Basic rolling-window circuit breaker

3 qualifying failures / 20 seconds
10-second cooldown
per-service state
normal 200 does not clear failures
threshold-producing request gets its real response
cooldown blocks subsequent calls
simple recovery attempt after cooldown
single-threaded assumption
```

## 34. Phase 2

```text
Add concurrency

thread-safe circuit lookup
atomic compound state transitions
CLOSED / OPEN / HALF_OPEN
exactly one recovery probe
competing HALF_OPEN callers fail fast
per-service synchronization
never hold circuit lock during network I/O
concurrent completion order may differ from start order
```

## 35. Phase 3

```text
Add response cache

cache before circuit breaker
(service, requestKey)
cache 200 only
30-second TTL
fresh hit skips breaker + downstream
fresh cache can serve while circuit is OPEN
cache hit does not update breaker health
thread-safe cache access
lazy expiry in simple implementation
unbounded-map risk
basic implementation does not prevent cache stampede
```

---

# Part IX — 30-Second Senior Explanation

> I started with independent circuit state per downstream service and tracked qualifying failures inside a rolling time window. Once the threshold is reached, subsequent calls fail fast during the cooldown. For concurrency, I make circuit lookup thread-safe and protect compound state transitions per service, with an explicit recovery-in-progress state so exactly one request can test recovery. I deliberately release the circuit lock before network I/O so normal calls are not unnecessarily serialized. Finally, I add a TTL response cache before circuit admission so fresh successful responses can be returned without contacting an unhealthy downstream service.

---

# Part X — HackerRank Discipline

When doing the real exercise:

1. Read the exact `execute(...)` signature first.
2. Read all supplied classes before writing new ones.
3. Identify what the prompt actually defines for a blocked call.
4. Do not invent `CircuitOpenException`, `503`, `null`, `Clock`, or extra APIs if the contract already tells you what to do.
5. Treat hidden tests as tests of the supplied contract, not of your preferred production API.
6. If the contract is genuinely silent in an interactive interview, say so and ask how the caller should observe a circuit-open rejection.
7. Keep the first implementation as small as possible while meeting the stated behaviour.
8. Explain data-structure and concurrency choices as you introduce them.
9. When requirements change, preserve earlier guarantees unless the interviewer explicitly changes them.

---

## Final Principle

> **If the interviewer could reasonably expect you to discover it, it belongs in the problem/TODOs—not in the starter boilerplate. If it is your design choice, it belongs in your explanation/answer—not disguised as something that was supplied.**
