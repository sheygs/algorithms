# Circuit Breaker — Phase 2

## Overview

Phase 2 evolves the Phase 1 circuit breaker into a **thread-safe, per-service state machine**.

The breaker still uses the same functional rules:

- `200` is success.
- `500` is a qualifying failure.
- 3 qualifying failures inside the active 20-second rolling window open the circuit.
- A normal `200` does not clear active failure history.
- The `OPEN` cooldown is 10 seconds.
- A successful recovery resets the breaker.
- A failed recovery immediately reopens the breaker and restarts the cooldown.

The main change is concurrency safety.

---

## Circuit States

Phase 2 introduces explicit states:

```java
enum CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
```

### `CLOSED`

Normal requests are allowed.

### `OPEN`

Requests fail fast while the cooldown is active.

### `HALF_OPEN`

Exactly one recovery request is currently testing the downstream service.

All competing callers are rejected until that recovery request completes.

---

## Admission Results

Admission is represented explicitly:

```java
enum Admission {
    NORMAL,
    RECOVERY_PROBE,
    REJECTED
}
```

### `NORMAL`

The circuit is `CLOSED` and the request may proceed normally.

### `RECOVERY_PROBE`

The request atomically transitioned:

```text
OPEN → HALF_OPEN
```

and is the only recovery request allowed through.

### `REJECTED`

The caller must fail fast without invoking the downstream service.

---

## Thread-Safe Circuit Registry

Phase 2 uses:

```java
ConcurrentHashMap<String, ServiceCircuit>
```

This protects concurrent circuit lookup and creation.

Example:

```java
ServiceCircuit circuit =
        circuits.computeIfAbsent(service, k -> new ServiceCircuit());
```

`ConcurrentHashMap` protects the map, but compound state transitions inside each `ServiceCircuit` are protected separately.

---

## Per-Service Synchronization

State-changing operations are synchronized on each individual `ServiceCircuit`.

Typical synchronized methods are:

```java
synchronized Admission admit(...)

synchronized void handleSuccess(...)

synchronized void handleFailure(...)
```

This makes compound operations atomic for one downstream service.

Importantly:

```text
Service B circuit lock != Service C circuit lock
```

Heavy activity for one service does not unnecessarily block another service.

---

## Admission Flow

The admission operation owns:

```text
prune active failures
→ inspect current state
→ check cooldown
→ perform OPEN → HALF_OPEN if eligible
→ return admission result
```

Conceptually:

```text
CLOSED
  → NORMAL

OPEN + cooldown active
  → REJECTED

OPEN + cooldown expired
  → HALF_OPEN
  → RECOVERY_PROBE

HALF_OPEN
  → REJECTED
```

Because this whole decision is synchronized, only one caller can become the recovery probe.

---

## Network Call Must Stay Outside the Lock

The downstream call is deliberately not executed inside a synchronized circuit method.

The lifecycle is:

```text
lock circuit
    ↓
make admission decision
    ↓
release lock
    ↓
call downstream
    ↓
capture outcome time
    ↓
lock circuit again
    ↓
apply success/failure result
    ↓
release lock
```

This prevents slow network I/O from serializing otherwise independent requests.

---

## Successful Response Handling

### Normal `200`

If:

```java
admission == Admission.NORMAL
```

the breaker does nothing.

Previous active failures remain in the rolling window.

### Recovery `200`

If:

```java
admission == Admission.RECOVERY_PROBE
```

the breaker performs:

```text
HALF_OPEN → CLOSED
```

and resets:

```java
failureTimes.clear();
openedAt = null;
state = CircuitState.CLOSED;
```

---

## Failure Handling

### Normal `500`

For a normally admitted request:

1. Capture `outcomeTime` after the downstream call.
2. Prune expired failures using `outcomeTime`.
3. Record the new failure.
4. Open the circuit only when:

```text
state == CLOSED
AND
failureTimes.size() >= FAILURE_THRESHOLD
```

The `state == CLOSED` guard matters because another concurrent request may already have opened the circuit while this request was in flight.

---

### Recovery `500`

A failed recovery performs:

```text
HALF_OPEN → OPEN
```

Then:

```java
openedAt = outcomeTime;
failureTimes.clear();
failureTimes.addLast(outcomeTime);
```

The failed probe becomes the first failure in a fresh failure history and the cooldown restarts immediately.

---

## Concurrent Failure Pruning

Phase 1 could assume that timestamps were inserted in chronological order.

Phase 2 cannot.

Concurrent requests can finish in a different order from the order in which they started.

Therefore pruning checks all retained timestamps:

```java
failureTimes.removeIf(
        t -> !t.isAfter(cutOff)
);
```

This is executed while holding the per-circuit lock.

---

## Core Production Limitations and Improvements

### Issue 1: Circuit state is local to one JVM instance

Each application instance maintains its own:

```java
ConcurrentHashMap<String, ServiceCircuit>
```

If the service runs on multiple pods or servers, each instance has an independent circuit state.

One instance may consider a downstream service `OPEN` while another still considers it `CLOSED`.

**Solution 1: Keep breakers instance-local unless shared coordination is genuinely required**

Instance-local breakers are usually desirable because they remain fast and do not depend on another remote system.

Use centralized metrics and monitoring to observe breaker behaviour across instances.

Only introduce distributed breaker state when coordinated state is an explicit requirement.

---

### Issue 2: Circuit-breaker policies are hard-coded

The current implementation uses fixed values such as:

```text
failure threshold = 3
failure window = 20 seconds
cooldown = 10 seconds
```

Different downstream services may require different policies.

**Solution 2: Make breaker configuration external and service-specific**

Move thresholds, rolling-window duration, cooldown, and related rules into application configuration.

Allow different downstream services to use different policies when appropriate.

---

### Issue 3: Only returned HTTP `500` responses are treated as failures

Real downstream failures include more than a single status code.

Examples include:

```text
timeouts
connection failures
502 / 503 / 504
DNS/network exceptions
```

**Solution 3: Add configurable failure classification**

Introduce a failure classifier that decides which responses and exceptions affect breaker health.

For example:

```text
500 / 502 / 503 / 504 → qualifying failure
timeout                → qualifying failure
connection exception   → qualifying failure
400 validation error   → usually not a breaker failure
```

---

### Issue 4: There is no timeout or bulkhead protection

A circuit breaker stops new calls after the dependency is considered unhealthy.

It does not prevent a large number of requests from already being in flight while the circuit is still `CLOSED`.

**Solution 4: Combine the breaker with timeouts and bulkheads**

Use the circuit breaker together with:

```text
request/connect/read timeouts
connection-pool limits
bulkhead/concurrency limits
```

A circuit breaker prevents repeated calls to an unhealthy dependency.

A timeout prevents a single call from hanging indefinitely.

A bulkhead prevents one dependency from consuming all available threads or connections.

---

### Issue 5: The circuit registry has no lifecycle management

Every new service key can create another:

```java
ServiceCircuit
```

and the implementation does not remove unused entries.

If service identifiers are dynamic or unbounded, memory usage can grow over time.

**Solution 5: Bound or expire unused circuit entries**

For a small fixed set of downstream services, this may not matter.

For dynamic service keys, use a bounded registry, idle-entry expiration, or predefined breaker configurations.

---

### Issue 6: There is no production observability

The implementation performs important transitions such as:

```text
CLOSED → OPEN
OPEN → HALF_OPEN
HALF_OPEN → CLOSED
HALF_OPEN → OPEN
```

but currently exposes no operational metrics.

**Solution 6: Add metrics, structured logging and tracing**

Useful signals include:

```text
current circuit state
rejected request count
CLOSED → OPEN transitions
recovery probe successes
recovery probe failures
downstream failure rate
downstream latency
```

This makes breaker behaviour visible to dashboards and alerts.

---

## Phase 2 Summary

```text
ConcurrentHashMap
        ↓
per-ServiceCircuit synchronization
        ↓
CLOSED / OPEN / HALF_OPEN
        ↓
atomic admission
        ↓
exactly one recovery probe
        ↓
network call outside lock
        ↓
synchronized outcome handling
```

Phase 2 solves the core concurrency problems from Phase 1 while preserving the same circuit-breaker business semantics.

For production, the next improvements are mainly around configurability, failure classification, timeouts and bulkheads, observability, registry lifecycle, and operational integration.
