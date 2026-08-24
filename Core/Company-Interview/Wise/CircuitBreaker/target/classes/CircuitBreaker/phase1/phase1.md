# Circuit Breaker — Phase 1

## Overview

Phase 1 implements a **single-threaded, per-service circuit breaker** using a rolling failure window.

Each downstream service has its own circuit state. A service is blocked after **3 qualifying `500` responses within an active 20-second rolling window**.

The circuit then stays blocked for **10 seconds**. Once the cooldown expires, the next request is allowed through as a recovery attempt.

---

## Behaviour

### CLOSED / normal operation

Phase 1 does not use an explicit state enum.

The state is represented by:

```java
blockedAt == null
```

Normal requests are sent downstream.

A `500` response is recorded in the rolling failure window.

When the number of active failures reaches 3:

```text
failureTimes.size() >= 3
```

the service becomes blocked:

```java
blockedAt = failureTime;
```

The third failing request still receives its real downstream `500` response.

---

### OPEN / blocked operation

A blocked service is represented by:

```java
blockedAt != null
```

While:

```text
now < blockedAt + 10 seconds
```

the request fails fast and the downstream service is not called.

At exactly:

```text
blockedAt + 10 seconds
```

the cooldown has expired and the next request is allowed through as a recovery attempt.

---

### Recovery success

If the recovery request returns `200`:

```java
blockedAt = null;
failureTimes.clear();
```

The breaker resets completely.

---

### Recovery failure

If the recovery request returns `500`:

1. Capture a new failure timestamp after the downstream call.
2. Prune expired failures.
3. Record the failed recovery.
4. Set `blockedAt` to the new failure time.
5. Restart the 10-second cooldown immediately.

The recovery request does not need to accumulate another three failures before the service is blocked again.

---

## Rolling Failure Window

The active failure window is:

```text
(now - 20 seconds, now]
```

A failure exactly 20 seconds old is expired.

In Phase 1, failures are recorded sequentially, so the timestamp deque remains chronologically ordered and expired entries can be removed from the front.

---

## Core Data Structures

```java
Map<String, ServiceCircuit> circuits = new HashMap<>();
```

Each `ServiceCircuit` contains:

```java
Deque<Instant> failureTimes = new ArrayDeque<>();
Instant blockedAt;
```

---

## Core Limitations and Production Improvements

### Issue 1: `HashMap` is not thread-safe

Phase 1 uses a regular `HashMap` for per-service circuit state.

This is safe only because Phase 1 assumes single-threaded execution. A production service may process many requests concurrently.

**Solution 1: Use a concurrency-safe circuit registry**

Use:

```java
ConcurrentHashMap<String, ServiceCircuit>
```

for thread-safe circuit lookup and creation.

However, a thread-safe map alone does not make the mutable state inside each `ServiceCircuit` thread-safe.

---

### Issue 2: Circuit state transitions are not atomic

Operations such as:

```text
check blocked state
→ check cooldown
→ decide recovery
→ update state
```

are performed without synchronization.

With concurrent callers, multiple threads could observe an expired cooldown and all perform recovery calls.

**Solution 2: Synchronize compound circuit-state operations**

Protect state inspection and state transitions with synchronization scoped to each `ServiceCircuit`.

This keeps state changes atomic without introducing a global lock across unrelated downstream services.

---

### Issue 3: There is no explicit `HALF_OPEN` state

Phase 1 represents recovery as a request-local decision after cooldown.

That is sufficient for a single-threaded implementation, but it cannot represent:

```text
one recovery request is already in flight
```

**Solution 3: Introduce an explicit state machine**

Use:

```text
CLOSED
OPEN
HALF_OPEN
```

`HALF_OPEN` represents exactly one recovery probe currently testing the downstream service.

Competing callers can then fail fast while the recovery probe is in progress.

---

### Issue 4: Failure pruning assumes timestamp insertion order

Phase 1 can safely remove expired timestamps from the front because requests complete sequentially.

Under concurrency, downstream calls can finish in a different order, so insertion order is no longer guaranteed to match timestamp order.

**Solution 4: Remove all expired timestamps instead of relying on deque order**

For example:

```java
failureTimes.removeIf(t -> !t.isAfter(cutOff));
```

The cleanup must happen while holding the circuit's synchronization lock.

---

## Phase 1 Summary

```text
Single-threaded
    ↓
HashMap
    ↓
blockedAt == null / non-null
    ↓
3 failures in 20 seconds
    ↓
10-second cooldown
    ↓
one simple recovery attempt
```

Phase 1 is useful for understanding the core circuit-breaker behaviour, but it is not safe for concurrent production traffic.

Phase 2 keeps the same breaker semantics while making circuit lookup, admission, state transitions, and failure handling concurrency-safe.
