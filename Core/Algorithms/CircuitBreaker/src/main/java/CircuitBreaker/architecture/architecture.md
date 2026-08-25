# Architecture Design Flow

## Phase 1

### Main Execution Flow

```mermaid
flowchart TD
    A["execute service"] --> B["validate service"]
    B --> C["get or create ServiceCircuit<br/>from HashMap"]
    C --> D["get current time<br/>Instant now"]
    D --> E["prune failures outside<br/>rolling 20s window"]
    E --> F["recoveryAttempt = false"]
    F --> G{"blockedAt not null"}

    G -- No --> L["call downstream"]
    G -- Yes --> H["calculate unblockAt<br/>blockedAt plus 10s"]
    H --> I{"now before unblockAt"}
    I -- Yes --> J["fail fast<br/>do not call downstream<br/>throw CircuitOpenException"]
    I -- No --> K["recoveryAttempt = true"]
    K --> L

    L --> M{"response code"}
    M -- 200 --> N["handleSuccessfulResponse"]
    M -- 500 --> O["handleFailure"]

    N --> P{"recoveryAttempt"}
    P -- Yes --> Q["reset<br/>clear failures<br/>clear blockedAt"]
    P -- No --> R["return response"]
    Q --> R

    O --> S["capture failureTime<br/>Instant now after call"]
    S --> T["prune failures again<br/>using failureTime"]
    T --> U["record failure"]
    U --> V{"recoveryAttempt OR<br/>failures at least 3"}
    V -- Yes --> W["block service<br/>blockedAt = failureTime"]
    V -- No --> R
    W --> R

    classDef terminal fill:#f8d7da,stroke:#c0392b,color:#000
    classDef success fill:#d4edda,stroke:#27ae60,color:#000
    class J terminal
    class R success
```

### Circuit Representation

```mermaid
flowchart LR
    N["Normal operation<br/>blockedAt is null"] -- "3rd qualifying 500<br/>within rolling 20s window" --> B["Blocked<br/>blockedAt is not null<br/>until blockedAt plus 10s"]
    B -- "cooldown expires<br/>recoveryAttempt succeeds 200" --> N
    B -- "cooldown expires<br/>recoveryAttempt fails 500<br/>blockedAt reset to new failureTime" --> B

    classDef normal fill:#d4edda,stroke:#27ae60,color:#000
    classDef blocked fill:#f8d7da,stroke:#c0392b,color:#000
    class N normal
    class B blocked
```

> Note: cooldown expiry does not change state by itself — it only flips `recoveryAttempt = true` on the next incoming request. There is no explicit `HALF_OPEN` state.

### Recovery Flow

```mermaid
flowchart TD
    A["Blocked service"] --> B["10 second cooldown expires"]
    B --> C["allow next request<br/>recoveryAttempt = true"]
    C --> D["call downstream"]
    D --> E{"response code"}
    E -- 200 --> F["reset breaker<br/>clear failures<br/>blockedAt = null"]
    E -- 500 --> G["record failure<br/>block again<br/>blockedAt = failureTime"]
    F --> H["continue normal operation"]
    G --> H

    classDef success fill:#d4edda,stroke:#27ae60,color:#000
    classDef fail fill:#f8d7da,stroke:#c0392b,color:#000
    class F success
    class G fail
```

## Phase 2

### Main Execution Flow

```mermaid
flowchart TD
    A["execute service"] --> B["get or create ServiceCircuit<br/>from ConcurrentHashMap"]
    B --> C["get current time from Clock"]
    C --> D["circuit.admit now<br/>synchronized per circuit"]

    D --> E{"admission result"}
    E -- "NORMAL" --> H["call downstream<br/>no circuit lock held"]
    E -- "RECOVERY_PROBE" --> H
    E -- "REJECTED" --> F["fail fast<br/>throw CircuitOpenException"]

    H --> I["capture outcomeTime"]
    I --> J{"response code"}
    J -- 200 --> K["handleSuccessfulResponse<br/>synchronized per circuit"]
    J -- 500 --> L["handleFailure<br/>synchronized per circuit"]

    K --> M{"was this a recoveryProbe"}
    M -- Yes --> N["HALF_OPEN to CLOSED<br/>clear failures<br/>clear openedAt"]
    M -- No --> O["keep old failure history"]

    L --> P{"was this a recoveryProbe"}
    P -- Yes --> Q["HALF_OPEN to OPEN<br/>openedAt = outcomeTime<br/>clear old failures<br/>record probe failure<br/>restart cooldown"]
    P -- No --> R["prune failures"]
    R --> S["record failure"]
    S --> T{"state is CLOSED<br/>AND failures at least 3"}
    T -- Yes --> U["CLOSED to OPEN<br/>openedAt = outcomeTime"]
    T -- No --> V["no state change"]

    N --> W["return response"]
    O --> W
    Q --> W
    U --> W
    V --> W

    classDef terminal fill:#f8d7da,stroke:#c0392b,color:#000
    classDef success fill:#d4edda,stroke:#27ae60,color:#000
    class F terminal
    class W success
```

### State Machine

```mermaid
flowchart LR
    CLOSED["CLOSED<br/>normal requests allowed"]
    OPEN["OPEN<br/>fail fast during cooldown"]
    HALF["HALF_OPEN<br/>exactly one recovery probe"]

    CLOSED -- "3 active failures<br/>within rolling 20s window" --> OPEN
    OPEN -- "10 second cooldown expires<br/>first admit call transitions" --> HALF
    HALF -- "recovery probe returns 200" --> CLOSED
    HALF -- "recovery probe returns 500" --> OPEN

    classDef closed fill:#d4edda,stroke:#27ae60,color:#000
    classDef open fill:#f8d7da,stroke:#c0392b,color:#000
    classDef half fill:#fff3cd,stroke:#d39e00,color:#000
    class CLOSED closed
    class OPEN open
    class HALF half
```

### Concurrency — Cooldown Expiry With 20 Simultaneous Requests

```mermaid
flowchart TD
    A["circuit becomes OPEN"] --> B["10 seconds pass"]
    B --> C["20 requests arrive together"]
    C --> D["circuit.admit now<br/>synchronized per circuit"]

    D --> E["one thread sees OPEN<br/>with cooldown complete"]
    D --> F["other 19 threads<br/>wait for or enter admit"]

    E --> G["OPEN to HALF_OPEN"]
    G --> H["admission result:<br/>RECOVERY_PROBE"]
    H --> I["leave circuit lock"]
    I --> J["make recovery call<br/>no circuit lock held"]

    F --> K["see HALF_OPEN"]
    K --> L["admission result:<br/>REJECTED"]
    L --> M["fail fast"]

    classDef probe fill:#fff3cd,stroke:#d39e00,color:#000
    classDef rejected fill:#f8d7da,stroke:#c0392b,color:#000
    class J probe
    class M rejected
```

## Phase 3

### Main Execution Flow

```mermaid
flowchart TD
    start(["execute(service, requestKey)"]) --> buildKey["Build CacheKey(service, requestKey)"]
    buildKey --> getTime["Get current time from Clock"]
    getTime --> lookupCache{"Lookup cache"}

    lookupCache -->|fresh| freshReturn["Return cached response"]
    lookupCache -->|"miss / expired"| removeExpired["Remove expired entry"]

    removeExpired --> getCircuit["Get/create ServiceCircuit<br/>from ConcurrentHashMap"]
    getCircuit --> admit["circuit.admit(current time)<br/>[synchronized per circuit]"]

    admit --> decision{"Admission result"}
    decision -->|NORMAL| leaveLock["Leave circuit lock"]
    decision -->|RECOVERY_PROBE| probeTransition["OPEN → HALF_OPEN<br/>recoveryProbe = true"]
    probeTransition --> leaveLock
    decision -->|REJECTED| rejectFail["Fail fast:<br/>throw CircuitOpenException"]

    leaveLock --> invokeCall["call()<br/>NO CIRCUIT LOCK HELD"]
    invokeCall --> outcome["Capture outcomeTime"]
    outcome --> statusCheck{"Downstream status"}

    statusCheck -->|200| success["handleSuccessfulResponse()<br/>[synchronized per circuit]"]
    statusCheck -->|500| failure["handleFailure()<br/>[synchronized per circuit]"]

    success --> successProbe{"recoveryProbe?"}
    successProbe -->|YES| successProbeYes["HALF_OPEN → CLOSED<br/>clear failures<br/>clear openedAt"]
    successProbe -->|NO| successProbeNo["Keep existing failure history"]

    failure --> failureProbe{"recoveryProbe?"}
    failureProbe -->|YES| failureProbeYes["HALF_OPEN → OPEN<br/>openedAt = outcomeTime<br/>clear old failures<br/>record probe failure<br/>restart cooldown"]
    failureProbe -->|NO| pruneFailures["Prune expired failures"]
    pruneFailures --> recordFailure["Record new failure"]
    recordFailure --> openCheck{"state == CLOSED AND<br/>failures >= 3?"}
    openCheck -->|YES| openTransition["CLOSED → OPEN<br/>openedAt = outcomeTime"]
    openCheck -->|NO| noChange["No state change"]

    successProbeYes --> respCheck{"response status == 200?"}
    successProbeNo --> respCheck
    failureProbeYes --> respCheck
    openTransition --> respCheck
    noChange --> respCheck

    respCheck -->|YES| cacheIt["Put successful response in cache<br/>expiresAt = outcomeTime + 30s"]
    respCheck -->|NO| returnResp["Return response"]
    cacheIt --> returnResp
```

### Cache Rules

```mermaid
flowchart LR
    r200["200 response"] --> cache30["Cache it for 30 seconds"]
    r500["500 response"] --> noCache["Never cache it"]
```

```mermaid
flowchart TD
    fresh["Fresh cache entry"] --> retImmediate["Return immediately"]
    retImmediate --> noTouch["Do not touch circuit breaker<br/>or downstream service"]

    expired["Expired cache entry"] --> notServe["Do not serve it"]
    notServe --> remove["Remove expired entry"]
    remove --> cont["Continue to circuit breaker"]
```

### Circuit State Machine

```mermaid
stateDiagram-v2
    [*] --> CLOSED
    CLOSED --> OPEN: 3 active failures
    OPEN --> HALF_OPEN: 10-second cooldown expires
    HALF_OPEN --> CLOSED: 200 (success)
    HALF_OPEN --> OPEN: 500 (failure)
```

### Description

Stage 3 places a thread-safe 30-second TTL response cache in front of the Stage 2 circuit breaker.

A fresh cached `200` response is returned immediately without consulting the circuit breaker or calling the downstream service. Cache misses and expired entries fall through to the breaker. Circuit admission and state transitions remain synchronized per downstream service, while the remote call itself is always performed outside the circuit lock. Only successful `200` responses are cached.

### Assumptions

#### 1. Stage 1 and Stage 2 breaker behaviour remains unchanged

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

- Stage 3 always checks the response cache before calling `ServiceCircuit.admit(...)`.
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
- There is no active background TTL sweep in this Stage 3 implementation.
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

```mermaid
flowchart TD
    lockA["Lock circuit"] --> decide["Make admission decision"]
    decide --> release["Release circuit lock"]
    release --> callDS["Call downstream service"]
    callDS --> capture["Capture outcomeTime"]
    capture --> lockB["Lock circuit again"]
    lockB --> update["Update breaker state"]
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
- If another request opens the circuit while they are in flight, Stage 3 does not cancel those existing calls.
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
