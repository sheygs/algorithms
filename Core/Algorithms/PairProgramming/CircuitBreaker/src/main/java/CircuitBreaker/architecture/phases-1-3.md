# Circuit Breaker Implementation Phases

## Phase 1 — Basic Circuit Breaker

### Main Execution Flow

```text
execute(service)
      │
      ▼
validate service
      │
      ▼
get/create ServiceCircuit from HashMap
      │
      ▼
get current time using Instant.now()
      │
      ▼
prune failures outside rolling 20-second window
      │
      ▼
recoveryAttempt = false
      │
      ▼
is blockedAt != null?
      │
      ├── NO ───────────────────────────────────────┐
      │                                             │
      └── YES                                       │
           │                                        │
           ▼                                        │
     calculate unblockAt                            │
     blockedAt + 10s                                │
           │                                        │
           ▼                                        │
     is now before unblockAt?                       │
           │                                        │
           ├── YES                                  │
           │    │                                   │
           │    ▼                                   │
           │  fail fast                             │
           │  DO NOT call()                         │
           │  throw CircuitOpenException            │
           │                                        │
           │  [request ends]                        │
           │                                        │
           └── NO                                   │
                │                                   │
                ▼                                   │
          recoveryAttempt = true                    │
                │                                   │
                └───────────────────────────────────┘
                                  │
                                  ▼
                              request admitted
                                  │
                                  ▼
                                call()
                                  │
                                  ▼
                         response status?
                                  │
                ┌─────────────────┴─────────────────┐
                │                                   │
              200                                 500
                │                                   │
                ▼                                   ▼
     handleSuccessfulResponse                  handleFailure
                │                                   │
                ▼                                   ▼
         recoveryAttempt?                   capture failureTime
            │        │                      after downstream call
         YES│        │NO                            │
            │        │                              ▼
            ▼        ▼                     prune failures again
          reset()  keep existing            using failureTime
            │      failure history                  │
            ▼                                       ▼
     clear failures                            record failure
     clear blockedAt                                │
            │                                       ▼
            │                              recoveryAttempt OR
            │                              failures >= threshold?
            │                                  │          │
            │                               YES│          │NO
            │                                  │          │
            │                                  ▼          ▼
            │                             block service  keep state
            │                             blockedAt =
            │                             failureTime
            │                                  │          │
            ▼                                  ▼          ▼
     return response                     return response return response
```

### Circuit Representation

Phase 1 does not use explicit `CLOSED`, `OPEN`, or `HALF_OPEN` states.

```text
blockedAt == null
      │
      ▼
normal operation


blockedAt != null
      │
      ▼
service is blocked
      │
      ▼
until blockedAt + 10 seconds
```

After the 10-second cooldown expires, the next request is simply marked:

```text
recoveryAttempt = true
```

The implementation does not transition to an explicit `HALF_OPEN` state.

### Recovery Flow

```text
blocked service
      │
      ▼
10-second cooldown expires
      │
      ▼
allow next request
recoveryAttempt = true
      │
      ▼
    call()
      │
      ├── 200
      │    │
      │    ▼
      │  reset breaker
      │    │
      │    └───────────────┐
      │                    │
      └── 500              │
           │               │
           ▼               │
        record failure     │
           │               │
           ▼               │
        block again        │
        blockedAt =        │
        failureTime        │
           │               │
           ▼               │
        restart cooldown   │
           │               │
           └───────────────┘
                  │
                  ▼
               continue
```

### Description

Phase 1 implements a single-threaded, per-service circuit breaker using a rolling failure window.

Each downstream service has its own `ServiceCircuit`. Three qualifying `500` responses inside a rolling 20-second window block that service for 10 seconds. Requests arriving during the cooldown fail fast without calling the downstream service.

Once the cooldown expires, the next request is allowed through as a simple recovery attempt. A successful recovery resets the breaker. A failed recovery immediately blocks the service again and restarts the cooldown.

### Assumptions

#### 1. Downstream response semantics

- The supplied downstream simulation returns either `200` or `500`.
- `200` is treated as success.
- `500` is the only qualifying failure.

#### 2. Failure threshold and rolling window

- A service is blocked after any three qualifying failures inside a rolling 20-second window.
- Failures do not need to be consecutive.
- A normal successful `200` does not clear active failures.
- Failures stop counting only after they leave the rolling window.
- A failure exactly 20 seconds old is expired.
- The active window is:

```text
(now - 20 seconds, now]
```

#### 3. Per-service isolation

- Every downstream service has its own `ServiceCircuit`.
- Failures for one service do not affect another service.

Example:

```text
ServiceB -> ServiceCircuit B
ServiceC -> ServiceCircuit C
```

#### 4. Phase 1 uses `HashMap`

- Circuit state is stored in a regular `HashMap`.
- This is acceptable only because Phase 1 assumes single-threaded execution.
- Thread-safe map access is introduced later in Phase 2.

#### 5. No explicit circuit-state enum

Phase 1 represents breaker state using `blockedAt`:

```text
blockedAt == null     -> normal operation
blockedAt != null     -> blocked
```

- There is no explicit `CLOSED`, `OPEN`, or `HALF_OPEN` enum.
- Recovery is represented by the request-local `recoveryAttempt` boolean.

#### 6. Opening the circuit

- The request that produces failure number 3 still reaches the downstream service.
- Its real `500` response is returned.
- After that failure is recorded, `blockedAt` is set to its failure timestamp.
- Subsequent requests fail fast until the cooldown expires.

#### 7. Behaviour while blocked

- A blocked service remains blocked for 10 seconds.
- While:

```text
now < blockedAt + 10 seconds
```

the request fails fast.

- `call()` must not execute.
- This implementation throws `CircuitOpenException`.

#### 8. Exact cooldown boundary

- At exactly:

```text
blockedAt + 10 seconds
```

the next request is allowed through.

- This follows from the implementation using:

```text
now.isBefore(unblockAt)
```

- Therefore:
  - before the boundary -> reject;
  - exactly at the boundary -> allow.

#### 9. Recovery attempt

- Once the cooldown finishes, the next permitted request is marked:

```text
recoveryAttempt = true
```

- `blockedAt` is deliberately left unchanged while that request executes.
- Phase 1 does not coordinate an exactly-one recovery probe because execution is assumed to be single-threaded.

#### 10. Successful recovery

If a recovery attempt returns `200`:

```text
reset()
```

which:

- clears all failure timestamps;
- sets `blockedAt = null`;
- returns the breaker to normal operation.

#### 11. Normal successful responses do not reset the breaker history

- A normal `200` does not clear previous failures.
- For example:

```text
500
200
500
200
500
```

still opens the circuit if the three `500` responses all remain inside the active 20-second window.

#### 12. Failure time is captured after the downstream call

- Phase 1 does not reuse the earlier admission/check time for a failure.
- When a `500` is observed, it captures:

```text
failureTime = Instant.now()
```

after `call()` returns.

- This gives a more accurate timestamp when the downstream call itself takes time.

#### 13. Failure history is pruned twice when necessary

- Failures are pruned before deciding whether the service is blocked.
- If the downstream call returns `500`, failures are pruned again using the actual `failureTime`.
- This accounts for time that may have passed while `call()` was executing.

#### 14. Failed recovery behaviour

If a recovery attempt returns `500`:

1. prune expired failures using `failureTime`;
2. record the new failure;
3. block the service immediately;
4. set:

```text
blockedAt = failureTime
```

- The threshold does not need to be reached again.
- The failed recovery itself is enough to reopen/re-block the circuit.
- Unlike the later Phase 2 implementation, Phase 1 does not explicitly clear the previous failure history before recording the failed recovery.

#### 15. Normal failure handling

For a normal `500`:

1. prune expired failures;
2. record the new failure;
3. if the active failure count is at least 3, set:

```text
blockedAt = failureTime
```

#### 16. Failure deque ordering relies on single-threaded execution

- Failure timestamps are stored in an `ArrayDeque`.
- Phase 1 assumes timestamps are inserted oldest to newest.
- Because execution is single-threaded, failures can safely be expired from the front of the deque.

The cleanup logic is effectively:

```text
while oldest failure <= cutoff
    remove oldest failure
```

- This ordering assumption is intentionally no longer relied upon once concurrency is introduced in Phase 2.

#### 17. Phase 1 is not thread-safe

The following are intentionally outside Phase 1 scope:

- concurrent access;
- `ConcurrentHashMap`;
- synchronized circuit operations;
- an explicit `HALF_OPEN` state;
- exactly-one recovery-probe coordination.

#### 18. Supplied-code boundary

- `call()` is treated as supplied/opaque downstream behaviour.
- Circuit-breaker logic belongs around `call()`.
- The implementation uses:

```java
execute(String service)
```

so no separate request object or request key is required.

#### 19. Demo `main()` is sequential

- `main()` alternates simulated calls between Service B and Service C.
- `Thread.sleep(2000)` only spaces calls over time.
- It does not introduce concurrent execution.

### Key Configuration

```text
Failure threshold : 3 failures
Rolling window    : 20 seconds
Cooldown          : 10 seconds
State isolation   : Per downstream service
Map               : HashMap
Thread model      : Single-threaded
Recovery model    : Simple recoveryAttempt boolean
Explicit HALF_OPEN: No
```

## Phase 2 — Thread-Safe Circuit Breaker

### Main Execution Flow

```text
execute(service)
      │
      ▼
get/create ServiceCircuit from ConcurrentHashMap
      │
      ▼
get current time from Clock
      │
      ▼
circuit.admit(now)
[synchronized per circuit]
      │
      ├── REJECTED
      │      │
      │      ▼
      │   fail fast
      │   throw CircuitOpenException
      │
      │   [request ends]
      │
      ├── NORMAL ────────────────────────────────┐
      │                                          │
      └── RECOVERY_PROBE                         │
             │                                   │
             ▼                                   │
       OPEN -> HALF_OPEN                         │
       recoveryProbe = true                      │
             │                                   │
             └───────────────────────────────────┘
                              │
                              ▼
                       request admitted
                              │
                              ▼
                            call()
                     NO CIRCUIT LOCK HELD
                              │
                              ▼
                     capture outcomeTime
                              │
                              ▼
                       response status?
                              │
      ┌───────────────────────┴───────────────────────┐
      │                                               │
      ▼                                               ▼
     200                                             500
      │                                               │
      ▼                                               ▼
handleSuccessfulResponse                         handleFailure
[synchronized per circuit]                 [synchronized per circuit]
      │                                               │
      ▼                                               ▼
recoveryProbe?                                  recoveryProbe?
      │                                               │
      ├── YES                                         ├── YES
      │    │                                          │    │
      │    ▼                                          │    ▼
      │ HALF_OPEN -> CLOSED                           │ HALF_OPEN -> OPEN
      │    │                                          │    │
      │    ▼                                          │    ▼
      │ clear failures                                │ openedAt = outcomeTime
      │ clear openedAt                                │    │
      │    │                                          │    ▼
      │    ▼                                          │ clear old failures
      │ return response                               │    │
      │                                               │    ▼
      └── NO                                          │ record probe failure
           │                                          │    │
           ▼                                          │    ▼
        keep old                                      │ restart cooldown
        failure history                               │    │
           │                                          │    ▼
           ▼                                          │ return response
        return response                               │
                                                      └── NO
                                                           │
                                                           ▼
                                                        prune failures
                                                           │
                                                           ▼
                                                        record failure
                                                           │
                                                           ▼
                                                        state == CLOSED
                                                        AND failures >= 3?
                                                           │
                                                           ├── YES
                                                           │    │
                                                           │    ▼
                                                           │ CLOSED -> OPEN
                                                           │ openedAt = outcomeTime
                                                           │    │
                                                           │    ▼
                                                           │ return response
                                                           │
                                                           └── NO
                                                                │
                                                                ▼
                                                             keep current state
                                                                │
                                                                ▼
                                                             return response
```

### State Machine

```text
CLOSED
  │
  │ 3 active failures
  ▼
OPEN
  │
  │ 10-second cooldown expires
  ▼
HALF_OPEN
  │
  ├── 200 ───────────────► CLOSED
  │
  └── 500 ───────────────► OPEN
```

### Concurrency

```text
Circuit becomes OPEN
        │
        ▼
10 seconds pass
        │
        ▼
20 requests arrive together
        │
        ▼
circuit.admit(now)
[synchronized per circuit]
        │
        ├── ONE thread
        │      │
        │      ▼
        │   sees OPEN + cooldown complete
        │      │
        │      ▼
        │   OPEN -> HALF_OPEN
        │      │
        │      ▼
        │   Admission.RECOVERY_PROBE
        │      │
        │      ▼
        │   leave circuit lock
        │      │
        │      ▼
        │   make recovery call
        │   NO CIRCUIT LOCK HELD
        │
        └── other 19 threads
               │
               ▼
            enter admit() one at a time
               │
               ▼
            see HALF_OPEN
               │
               ▼
            REJECTED
               │
               ▼
            fail fast
```

### Description

Phase 2 turns the Phase 1 breaker into a thread-safe per-service state machine.

Requests are admitted through `ServiceCircuit.admit(now)`, which synchronizes state inspection and state transitions for that service. Requests run normally in `CLOSED`, fail fast in `OPEN`, and after the cooldown exactly one request atomically transitions the circuit from `OPEN` to `HALF_OPEN` and becomes the recovery probe. The downstream call is always performed outside the circuit lock, and the breaker state is synchronized again when the downstream result is applied.

### Assumptions

#### 1. Phase 1 behaviour remains unchanged

- `200` is a success.
- `500` is the only qualifying failure.
- Three qualifying failures inside the active rolling 20-second window open the circuit.
- Failures do not have to be consecutive.
- A normal successful `200` does not clear previous active failures.
- A failure exactly 20 seconds old is expired.
- The active window is:

```text
(now - 20 seconds, now]
```

- Circuit-breaker state is independent per downstream service.
- The `OPEN` cooldown is 10 seconds.

#### 2. Time handling

- Time is read through an injected `Clock`.
- Production uses the real system clock.
- Tests can use a controlled clock.
- This allows exact rolling-window and cooldown boundaries to be tested without relying on `Thread.sleep()`.

#### 3. Explicit circuit states

The breaker has three states:

```text
CLOSED
OPEN
HALF_OPEN
```

- `CLOSED` means normal requests are allowed.
- `OPEN` means requests fail fast while the cooldown is active.
- `HALF_OPEN` means exactly one recovery request is currently testing the downstream service.

#### 4. Admission is encapsulated in `ServiceCircuit.admit(now)`

- `execute()` does not directly manipulate the breaker state during admission.
- `ServiceCircuit.admit(now)` owns:
  - rolling-window pruning;
  - current-state inspection;
  - cooldown checks;
  - `OPEN -> HALF_OPEN` transition;
  - deciding whether the request is admitted or rejected.
- `admit(now)` is synchronized per `ServiceCircuit`.

#### 5. Admission results are explicit

`admit(now)` returns one of:

```text
NORMAL
RECOVERY_PROBE
REJECTED
```

- `NORMAL` means the request was admitted from `CLOSED`.
- `RECOVERY_PROBE` means this request atomically changed `OPEN -> HALF_OPEN`.
- `REJECTED` means the request must fail fast.

#### 6. Recovery admission boundary

- At exactly:

```text
openedAt + 10 seconds
```

the cooldown has finished.

- The first request entering `admit(now)` may transition:

```text
OPEN -> HALF_OPEN
```

- That request becomes the one recovery probe.
- While the circuit is `HALF_OPEN`, all other callers are rejected.

#### 7. Circuit rejection behaviour

- A rejected request must not call the downstream service.
- This implementation throws `CircuitOpenException`.
- Rejection occurs when:
  - the circuit is `OPEN` and the cooldown has not finished; or
  - the circuit is already `HALF_OPEN` because another recovery probe is in flight.

#### 8. Successful recovery

When the recovery probe returns `200`:

```text
HALF_OPEN -> CLOSED
```

The breaker is reset:

- clear previous failure timestamps;
- clear `openedAt`;
- set state to `CLOSED`.

#### 9. Failed recovery

When the recovery probe returns `500`:

```text
HALF_OPEN -> OPEN
```

The implementation:

- sets `openedAt = outcomeTime`;
- clears the previous failure history;
- records the failed probe as the first failure in the new history;
- restarts the 10-second cooldown from `outcomeTime`.

#### 10. Normal successful responses do not reset failure history

- A normal `200` while operating in `CLOSED` does not clear existing active failures.
- Only a successful recovery probe performs a full reset.

#### 11. Normal `500` handling

For a non-recovery `500`, the breaker:

1. prunes failures using `outcomeTime`;
2. records the new failure;
3. opens the circuit only when:

```text
state == CLOSED && failures >= 3
```

- The request that creates failure number 3 still receives the real downstream `500`.
- Future requests fail fast once the circuit is `OPEN`.

#### 12. Why `state == CLOSED` is checked again

- Multiple `CLOSED` requests may already be in flight concurrently.
- Another request may open the circuit while this request is making its downstream call.
- Therefore, a completed normal failure must only perform:

```text
CLOSED -> OPEN
```

if the circuit is still `CLOSED`.

#### 13. Admission and outcome updates are synchronized separately

The lifecycle is:

```text
lock circuit
     │
     ▼
admit request
     │
     ▼
release lock
     │
     ▼
call downstream
     │
     ▼
capture outcomeTime
     │
     ▼
lock circuit again
     │
     ▼
apply success/failure result
     │
     ▼
release lock
```

- Admission and breaker updates are atomic per service.
- The remote call itself is not made while holding the breaker lock.

#### 14. Per-service synchronization

- Synchronization is performed on each individual `ServiceCircuit`.
- Service B and Service C therefore have independent locks.
- A slow or busy Service B circuit does not unnecessarily block Service C's circuit operations.

#### 15. `ConcurrentHashMap` does not replace circuit locking

- `ConcurrentHashMap` makes access to the circuit map thread-safe.
- It does not make compound operations inside a `ServiceCircuit` atomic.
- Per-circuit synchronization is still required for state transitions and failure bookkeeping.

#### 16. Failure-window cleanup under concurrency

- The failure deque cannot safely rely on timestamps being inserted in chronological order when multiple requests complete concurrently.
- Expired failures are therefore removed by checking all timestamps rather than only removing from the front.
- Since the failure threshold is only three, the implementation accepts this small `O(n)` cleanup cost.

#### 17. In-flight CLOSED requests are not cancelled

- Multiple requests admitted while the circuit is `CLOSED` may already be calling the downstream service.
- If another request opens the circuit while they are in flight, those calls continue.
- Their results are handled normally when they complete.

#### 18. Thread safety is local to this JVM

- `ConcurrentHashMap` and per-circuit synchronization protect state only inside this process.
- There is no distributed circuit-breaker state shared across multiple JVM instances.

#### 19. Supplied downstream call remains opaque

- `call()` is treated as supplied remote-call behaviour.
- Circuit-breaker logic is implemented around it rather than inside it.

#### 20. Demo `main()` does not test concurrency

- The `main()` method uses `Thread.sleep()` only to space requests over time.
- It does not create concurrent request execution.
- Concurrency behaviour should be verified with dedicated concurrent tests.

#### Interview Answer

The circuit state is shared by all concurrent requests to the same downstream service. Both methods can mutate shared state such as the failure deque, circuit state, and open timestamp, so I synchronize them to make those state transitions atomic and prevent races. I synchronize at the individual ServiceCircuit level rather than globally, so requests to different services don't block each other. I also deliberately don't hold that lock during the downstream network call because that would unnecessarily serialize requests and reduce throughput

## Phase 3 — Circuit Breaker with Response Cache

### Main Execution Flow

```text
execute(service, requestKey)
      │
      ▼
build CacheKey(service, requestKey)
      │
      ▼
get current time from Clock
      │
      ▼
lookup cache
      │
      ├── FRESH
      │      │
      │      ▼
      │   return cached response
      │   DO NOT touch circuit breaker
      │   DO NOT call downstream service
      │
      │   [request ends]
      │
      └── MISS / EXPIRED
             │
             ▼
       remove expired entry if present
             │
             ▼
       get/create ServiceCircuit from ConcurrentHashMap
             │
             ▼
       circuit.admit(current time)
       [synchronized per circuit]
             │
             ├── REJECTED
             │      │
             │      ▼
             │   fail fast
             │   throw CircuitOpenException
             │
             │   [request ends]
             │
             ├── NORMAL ───────────────────────────────┐
             │                                         │
             └── RECOVERY_PROBE                        │
                    │                                  │
                    ▼                                  │
              OPEN -> HALF_OPEN                        │
              recoveryProbe = true                     │
                    │                                  │
                    └──────────────────────────────────┘
                                   │
                                   ▼
                            request admitted
                                   │
                                   ▼
                                 call()
                          NO CIRCUIT LOCK HELD
                                   │
                                   ▼
                          capture outcomeTime
                                   │
                                   ▼
                            response status?
                                   │
           ┌───────────────────────┴───────────────────────┐
           │                                               │
           ▼                                               ▼
          200                                             500
           │                                               │
           ▼                                               ▼
handleSuccessfulResponse                              handleFailure
[synchronized per circuit]                      [synchronized per circuit]
           │                                               │
           ▼                                               ▼
     recoveryProbe?                                  recoveryProbe?
           │                                               │
           ├── YES                                         ├── YES
           │    │                                          │    │
           │    ▼                                          │    ▼
           │ HALF_OPEN -> CLOSED                           │ HALF_OPEN -> OPEN
           │    │                                          │    │
           │    ▼                                          │    ▼
           │ clear failures                                │ openedAt = outcomeTime
           │ clear openedAt                                │    │
           │    │                                          │    ▼
           │    └──────────────┐                           │ clear old failures
           │                   │                           │    │
           └── NO              │                           │    ▼
                │              │                           │ record probe failure
                ▼              │                           │    │
             keep existing     │                           │    ▼
             failure history   │                           │ restart cooldown
                │              │                           │    │
                └──────────────┘                           │    ▼
                       │                                   │  DO NOT cache
                       ▼                                   │    │
          put successful response                          │    ▼
                in cache                                   │   return response
       expiresAt = outcomeTime + 30s                       │
                       │                                   └── NO
                       ▼                                      │
                 return response                              ▼
                                                          prune failures
                                                              │
                                                              ▼
                                                           record failure
                                                              │
                                                              ▼
                                                           state == CLOSED
                                                           AND failures >= 3?
                                                              │
                                                              ├── YES
                                                              │    │
                                                              │    ▼
                                                              │ CLOSED -> OPEN
                                                              │ openedAt = outcomeTime
                                                              │    │
                                                              │    ▼
                                                              │ DO NOT cache
                                                              │    │
                                                              │    ▼
                                                              │ return response
                                                              │
                                                              └── NO
                                                                   │
                                                                   ▼
                                                                keep current state
                                                                   │
                                                                   ▼
                                                                DO NOT cache
                                                                   │
                                                                   ▼
                                                                return response
```

### Cache Rules

```text
200 response
     │
     ▼
cache for 30 seconds


500 response
     │
     ▼
never cache
```

```text
fresh cache entry
        │
        ▼
return immediately
        │
        ▼
do not touch circuit breaker
or downstream service


expired cache entry
        │
        ▼
do not serve it
        │
        ▼
remove expired entry
        │
        ▼
continue to circuit breaker
```

### Circuit State Machine

```text
CLOSED
  │
  │ 3 active failures
  ▼
OPEN
  │
  │ 10-second cooldown expires
  ▼
HALF_OPEN
  │
  ├── 200 ───────────────► CLOSED
  │
  └── 500 ───────────────► OPEN
```

### Description

Phase 3 places a thread-safe 30-second TTL response cache in front of the Phase 2 circuit breaker.

A fresh cached `200` response is returned immediately without consulting the circuit breaker or calling the downstream service. Cache misses and expired entries fall through to the breaker. Circuit admission and state transitions remain synchronized per downstream service, while the remote call itself is always performed outside the circuit lock. Only successful `200` responses are cached.

### Assumptions

#### 1. Phase 1 and Phase 2 breaker behaviour remains unchanged

- `200` is treated as success.
- `500` is the only qualifying failure.
- Three qualifying failures within the active rolling 20-second window open the circuit.
- The active failure window is `(now - 20 seconds, now]`.
- A failure exactly 20 seconds old is expired.
- Circuit-breaker state is independent per downstream service.
- The `OPEN` cooldown is 10 seconds.
- Circuit states are explicit: `CLOSED`, `OPEN`, and `HALF_OPEN`.
- Exactly one request may transition `OPEN -> HALF_OPEN` and act as the recovery probe.
- While `HALF_OPEN`, other callers fail fast.
- A successful recovery probe performs `HALF_OPEN -> CLOSED`.
- A failed recovery probe performs `HALF_OPEN -> OPEN` and restarts the cooldown.

#### 2. Cache lookup happens before circuit-breaker admission

- Phase 3 always checks the response cache before calling `ServiceCircuit.admit(...)`.
- A fresh cache hit returns immediately.
- Therefore, a fresh cached response can still be served even when the service's circuit is `OPEN` or `HALF_OPEN`.
- On a fresh cache hit, neither the breaker nor the downstream service is touched.

#### 3. Cache entries are scoped by service and request key

- The cache key is:

```text
(service, requestKey)
```

- Two request keys for the same service are different cache entries.
- The same request key used for different services is also treated as a different cache entry.

Example:

```text
("ServiceB", "account-123")
```

is different from:

```text
("ServiceB", "account-456")
```

#### 4. Cache TTL is 30 seconds

- Successful responses remain fresh for 30 seconds.
- An entry is fresh only while:

```text
now < expiresAt
```

- At exactly `expiresAt`, the entry is expired.
- Expired entries are not served.

#### 5. Only successful responses are cached

- A downstream `200` response is cached.
- A downstream `500` response is never cached.
- The cache expiry timestamp is based on `outcomeTime`, the time at which the downstream result is observed.

#### 6. Cache eviction is lazy

- Expired entries are removed when that exact cache key is accessed again.
- There is no active background TTL sweep in this Phase 3 implementation.
- The cache is not bounded by a maximum size.
- For long-running production use, a bounded cache or active expiry mechanism would be required to avoid growth from one-off request keys.

#### 7. Expired cache removal is concurrency-safe

- When an expired entry is observed, the implementation removes only that exact cached value:

```text
cache.remove(cacheKey, cached)
```

- If another thread has already replaced the entry with a newer value, that newer value is not accidentally removed.

#### 8. Circuit admission is atomic per service

- `ServiceCircuit.admit(...)` is synchronized.
- State inspection, rolling-window cleanup, cooldown checks, and `OPEN -> HALF_OPEN` admission happen atomically for one service.
- Synchronization is per `ServiceCircuit`, so unrelated downstream services do not share the same lock.

#### 9. The downstream call is never made while holding the circuit lock

The execution order is:

```text
lock circuit
     │
     ▼
make admission decision
     │
     ▼
release circuit lock
     │
     ▼
call downstream service
     │
     ▼
capture outcomeTime
     │
     ▼
lock circuit again
     │
     ▼
update breaker state
     │
     ▼
release lock
```

- Network latency therefore does not keep the per-service circuit lock held.

#### 10. Breaker outcome handling is synchronized

- `handleSuccessfulResponse(...)` is synchronized.
- `handleFailure(...)` is synchronized.
- This means breaker state is protected both when a request is admitted and when its downstream result is applied.

#### 11. Normal successful responses do not clear failure history

- A normal `200` while the circuit is `CLOSED` does not remove previous active failures.
- Only a successful recovery probe resets the breaker and clears its previous failure history.

#### 12. Failed recovery starts a fresh failure history

When a `HALF_OPEN` recovery probe returns `500`:

```text
HALF_OPEN -> OPEN
```

and the implementation:

- sets `openedAt = outcomeTime`;
- clears the old failure history;
- records the failed recovery probe as the first failure in the new history;
- restarts the 10-second cooldown.

#### 13. Normal failures only perform `CLOSED -> OPEN`

For a non-recovery `500`, the breaker:

1. prunes expired failures using `outcomeTime`;
2. records the new failure;
3. opens the circuit only when:

```text
state == CLOSED && failures >= 3
```

This guard matters because another request may already have changed the circuit state while this request was in flight.

#### 14. Existing in-flight CLOSED requests are not cancelled

- Multiple `CLOSED` requests may already be calling the downstream service concurrently.
- If another request opens the circuit while they are in flight, Phase 3 does not cancel those existing calls.
- Their results are still processed when they complete.

#### 15. Time comes from an injected Clock

- Rolling-window boundaries, cooldown boundaries, and cache TTL boundaries all use the injected `Clock`.
- This allows exact time-dependent behaviour to be tested deterministically.

#### 16. Thread safety is local to this JVM

- `ConcurrentHashMap` protects the shared circuit/cache maps in this process.
- Per-service breaker transitions are synchronized inside `ServiceCircuit`.
- No distributed/shared circuit-breaker state is implemented across multiple JVM instances.

#### 17. Supplied downstream call remains opaque

- `call()` is treated as supplied remote-call behaviour.
- Circuit-breaker and caching logic are implemented around it rather than inside it.

#### 18. Backwards-compatible execute overload

The implementation also supports:

```java
execute(String service)
```

which delegates to:

```java
execute(service, service)
```

Therefore, when that overload is used, the service name also becomes the request cache key.
