# Money Transfer — Clean Starter Mock

## Purpose

This version is for practising the **clean-starter interview variant**.

Unlike the rough-code exercise, you are **not** given a broken implementation to fix. You are given a small domain model, a repository, and an exchange-rate provider, then asked to build the transfer flow incrementally.

The goal is to practise:

- establishing the correct business-flow order yourself;
- identifying validation and side-effect invariants;
- making small, safe changes as requirements are added;
- explaining trade-offs while coding;
- avoiding premature over-engineering.

---

## Timed Mock Rules

Treat this like a real pair-programming interview.

- Start from the starter code below.
- Do not copy the completed implementation from the rough-code exercise.
- Talk through assumptions before coding.
- Implement one TODO at a time.
- Keep the design simple until a requirement forces a change.
- Prefer correctness and clarity over adding production infrastructure.
- Suggested time: **45–60 minutes**.
- If you finish early, continue into the later follow-ups.

---

# Starter Code

```java
package CurrencyConverter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class TransferService {

    private final UserRepository userRepository;
    private final ExchangeRateProvider exchangeRateProvider;

    public TransferService(
            UserRepository userRepository,
            ExchangeRateProvider exchangeRateProvider) {

        this.userRepository = userRepository;
        this.exchangeRateProvider = exchangeRateProvider;
    }

    public TransferResult send(
            String transferId,
            String senderId,
            String recipientId,
            BigDecimal amount,
            String sourceCurrency,
            String targetCurrency) {

        // TODO: implement incrementally

        return null;
    }

    public BigDecimal receive(
            String userId,
            String currency) {

        User user = userRepository.findById(userId);

        return user.getAccount(currency)
                .getBalance();
    }
}


// ---------- Supplied domain code ----------

class User {

    private final String id;
    private final Map<String, Account> accounts = new HashMap<>();

    public User(String id) {
        this.id = id;
    }

    public Account getAccount(String currency) {
        return accounts.get(currency);
    }

    public void addAccount(Account account) {
        accounts.put(account.getCurrency(), account);
    }
}


class Account {

    private final String currency;
    private BigDecimal balance;

    public Account(
            String currency,
            BigDecimal balance) {

        this.currency = currency;
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }
}


// ---------- Supplied dependencies ----------

interface UserRepository {

    // Returns null when the user does not exist.
    User findById(String userId);
}


interface ExchangeRateProvider {

    // Treat this as a supplied external API.
    BigDecimal getRate(
            String fromCurrency,
            String toCurrency)
            throws ExchangeRateException;
}


class ExchangeRateException extends RuntimeException {

    public ExchangeRateException(String message) {
        super(message);
    }

    public ExchangeRateException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}


// ---------- Supplied result model ----------

record TransferResult(
        String transferId,
        TransferStatus status,
        BigDecimal sentAmount,
        String sourceCurrency,
        BigDecimal receivedAmount,
        String targetCurrency,
        BigDecimal exchangeRate) {
}


enum TransferStatus {
    SUCCESS
}
```

---

# Core Invariant

Keep this in mind throughout the exercise:

> **Do not modify either account until every prerequisite needed to safely complete the transfer has succeeded.**

A useful mental model is:

```text
validate
    ↓
load required state
    ↓
verify transfer can happen
    ↓
resolve conversion
    ↓
calculate final amount
    ↓
perform side effects
    ↓
return result
```

---

# TODO 1 — Basic Successful Transfer

Implement the happy path.

A successful transfer should:

1. find the sender;
2. find the recipient;
3. find the sender's `sourceCurrency` account;
4. find the recipient's `targetCurrency` account;
5. obtain the exchange rate;
6. calculate the converted amount;
7. debit the sender;
8. credit the recipient;
9. return a successful `TransferResult`.

Do not add caching, retries, concurrency handling, or persistence yet.

### Example

```text
sender balance = 500 GBP
recipient balance = 0 EUR
amount = 100 GBP
rate = 1.20
```

Expected:

```text
sender balance = 400 GBP
recipient balance = 120 EUR
```

---

# TODO 2 — Validate Arguments and Required Entities

Add safe failure handling.

Reject:

- null/blank `transferId`;
- null/blank `senderId`;
- null/blank `recipientId`;
- null/blank `sourceCurrency`;
- null/blank `targetCurrency`;
- null amount;
- zero amount;
- negative amount;
- missing sender;
- missing recipient;
- missing source account;
- missing target account.

### Required rule

All validation must happen before any account balance is changed.

---

# TODO 3 — Insufficient Funds

A transfer must not proceed when the sender does not have enough money.

### Requirements

- check the sender's balance before calling the exchange-rate provider;
- fail immediately when funds are insufficient;
- do not debit the sender;
- do not credit the recipient;
- do not call the provider unnecessarily.

### Example

```text
sender balance = 50 GBP
amount = 100 GBP
```

Expected:

```text
transfer fails
provider calls = 0
sender balance unchanged
recipient balance unchanged
```

---

# TODO 4 — Same-Currency Transfer

If:

```text
sourceCurrency == targetCurrency
```

do not call the exchange-rate provider.

Use:

```java
BigDecimal.ONE
```

as the effective exchange rate.

### Example

```text
100 GBP → GBP
```

Expected:

```text
sender debited 100 GBP
recipient credited 100 GBP
provider calls = 0
exchangeRate = 1
```

All existing validation rules must remain intact.

---

# TODO 5 — Money Precision and Rounding

The final amount credited to the recipient must respect the target currency's supported precision.

Use:

```java
Currency.getInstance(targetCurrency)
        .getDefaultFractionDigits();
```

Use:

```java
RoundingMode.HALF_EVEN
```

for this exercise.

### Rule

Do not round the exchange rate itself.

Calculate:

```text
amount × full-precision rate
```

then round the resulting monetary amount.

### Example

```text
100 GBP
rate = 1.1734567
target = EUR
```

Raw:

```text
117.3456700
```

Final:

```text
117.35 EUR
```

---

# TODO 6 — Validate Provider Responses

Treat a provider response as invalid when:

```text
rate == null
rate <= 0
```

### Requirements

An invalid rate must:

- fail the transfer;
- never be used;
- never modify either account balance.

Keep provider exceptions distinct from malformed provider responses.

---

# TODO 7 — Cache Exchange Rates

The exchange-rate provider is expensive.

Add a configurable TTL cache keyed by currency pair.

Suggested concepts:

```java
record CurrencyPair(
        String fromCurrency,
        String toCurrency) {
}
```

```java
record CachedRate(
        BigDecimal rate,
        Instant expiresAt) {
}
```

### Required behaviour

```text
no cache
    → provider
    → validate
    → cache
    → use

fresh cache
    → use cache
    → no provider call

expired cache
    → provider
    → validate
    → replace cache
    → use
```

### Rules

- cache by directional currency pair;
- `GBP → EUR` and `EUR → GBP` are different keys;
- do not cache null, zero, or negative rates;
- TTL must be configurable;
- same-currency transfers bypass the cache.

For this TODO, assume single-threaded execution.

---

# TODO 8 — Provider Failure + Stale-Rate Fallback

New policy:

> If the cached rate has expired and the provider fails, use the stale rate when one exists.

Required behaviour:

```text
fresh cache
    → use fresh rate

no cache
    → provider succeeds → use fresh rate
    → provider fails    → fail

stale cache
    → provider succeeds → replace stale rate
    → provider fails    → use stale rate
```

Do not overwrite the stale entry when the provider fails.

---

# TODO 9 — Idempotent Transfer

Clients may retry requests.

Use `transferId` so the same completed transfer does not execute twice.

### Requirements

- a successful transfer produces a `TransferResult`;
- store the original result against its `transferId`;
- a duplicate completed `transferId` returns the original result;
- do not debit again;
- do not credit again;
- do not call the provider again;
- failed transfers should not be recorded as completed.

For this TODO, keep the implementation single-threaded.

### Important invariant

> A transfer ID is completed only after the transfer side effects have succeeded.

---

# TODO 10 — Unsupported Currency

Reject unsupported currency codes before repository/provider work that is not necessary.

For this practice version, use Java's currency metadata:

```java
Currency.getInstance(currencyCode);
```

### Expected

```text
sourceCurrency = ABC
targetCurrency = EUR
```

Result:

```text
transfer rejected
provider not called
balances unchanged
```

---

# TODO 11 — Inject `Clock`

The TTL logic should not depend directly on wall-clock time.

Inject:

```java
Clock
```

into `TransferService`.

Replace:

```java
Instant.now()
```

with:

```java
Instant.now(clock)
```

### Freshness rule

```text
now < expiresAt
    → fresh

now >= expiresAt
    → expired
```

This should allow deterministic TTL tests without `Thread.sleep()`.

Also validate constructor configuration:

- repository is not null;
- provider is not null;
- TTL is not null;
- TTL is positive;
- Clock is not null.

---

# TODO 12 — Refactor Rate Resolution

Once the behaviour works, extract the exchange-rate responsibility from `send()`.

For example:

```java
private BigDecimal resolveRate(
        String sourceCurrency,
        String targetCurrency) {
    ...
}
```

It should own:

```text
cache lookup
    ↓
freshness decision
    ↓
provider lookup
    ↓
rate validation
    ↓
cache refresh
    ↓
stale fallback
```

The goal is to make `send()` read primarily as a transfer business flow.

Do not change behaviour during the refactor.

---

# TODO 13 — Focused Tests

Prioritise tests for behaviour that is easy to break:

- successful transfer;
- same-currency provider bypass;
- insufficient funds;
- missing sender/recipient/account;
- malformed provider rate;
- provider exception with no stale cache;
- fresh cache hit;
- exact TTL expiry boundary;
- stale-rate fallback;
- EUR/GBP-style rounding;
- JPY zero-decimal rounding;
- duplicate `transferId`;
- failed transfer does not alter balances.

Use fakes and an injected test clock.

---

# Later Follow-Ups

These are useful Senior-level follow-ups, but do not implement them unless time remains or the interviewer asks.

## TODO 14 — Missing Direct Rate / Cross-Rate

If:

```text
GBP → JPY
```

is unavailable directly, derive it through a configured base currency:

```text
GBP → USD
USD → JPY
```

Do not build an arbitrary graph/BFS unless explicitly required.

---

## TODO 15 — Idempotency Request Mismatch

Consider:

```text
TX-100
100 GBP → EUR
```

followed by:

```text
TX-100
500 GBP → EUR
```

The same idempotency key should not silently represent two materially different requests.

Production approach:

```text
transferId + request fingerprint
```

Same ID + same fingerprint:

```text
replay result
```

Same ID + different fingerprint:

```text
reject conflict
```

---

## TODO 16 — Concurrent Access

Discuss the difference between:

```text
thread-safe collection access
```

and:

```text
atomic business operations
```

A `ConcurrentHashMap` can make local map operations safe, but it does not make:

```text
check transfer
→ move money
→ store result
```

atomic.

Production idempotency should normally rely on a durable shared store with an atomic claim / unique `transferId`.

---

## TODO 17 — Cache Stampede

Several threads may simultaneously observe the same expired/missing currency pair and all call the external provider.

Discuss options such as:

- accepting occasional duplicate refreshes;
- per-key locking;
- request coalescing;
- distributed coordination when required.

Keep this separate from money-transfer idempotency.

---

## TODO 18 — Retry Policy

Classify errors before retrying.

Potentially retry:

```text
timeout
temporary network failure
provider 5xx/unavailable
```

Do not blindly retry:

```text
invalid currency
invalid amount
null/zero/negative provider rate
```

Discuss:

- bounded retry count;
- timeouts;
- exponential backoff;
- jitter;
- metrics;
- stale-rate fallback.

---

## TODO 19 — Cache Lifecycle

Discuss whether the cache needs:

- maximum size;
- expiry cleanup;
- eviction policy;
- a dedicated cache library;
- Redis/shared caching.

Do not add this complexity unless required.

---

# Production Review Questions

Be prepared to explain these without necessarily implementing them.

## 1. How would you make debit + credit atomic?

Expected direction:

```text
single database transaction
```

Either both changes commit or both roll back.

---

## 2. Why is in-memory idempotency insufficient?

Consider:

```text
application restart
multiple application instances
concurrent duplicate requests
```

Production direction:

```text
durable shared idempotency/transfer record
+
unique transferId
```

---

## 3. Why doesn't `ConcurrentHashMap` solve idempotency?

Because:

```text
get()
```

and:

```text
put()
```

may each be thread-safe while the complete:

```text
check
→ process transfer
→ store result
```

workflow still races.

---

## 4. Which state belongs in a database vs a cache?

```text
completed transfer / idempotency state
    → durable database

exchange rates
    → cache
```

Correctness-critical financial state should not depend on a disposable cache.

---

## 5. What happens in a multi-instance deployment?

Local memory is not shared:

```text
Server A → local map A
Server B → local map B
Server C → local map C
```

Discuss shared durable state for correctness and shared/distributed caching only where useful.

---

# Final Self-Review

Before ending the mock, ask yourself:

- Have I validated everything required before side effects?
- Can a failed rate lookup debit the sender?
- Can an invalid recipient/account cause a partial transfer?
- Am I using `BigDecimal`, not floating-point money arithmetic?
- Is money rounded at the correct boundary?
- Are same-currency transfers bypassing FX?
- Is the cache keyed by directional currency pair?
- Is expiry behaviour explicit?
- Can the provider failure path safely use or reject stale data?
- Can a duplicate transfer execute twice in the current exercise?
- What changes when concurrency or multiple service instances are introduced?
- Have I kept the implementation proportional to the requirements?

---

# Suggested Mock Progression

If you want the session to feel interviewer-driven, reveal the TODOs gradually.

```text
Round 1
Basic successful transfer

Round 2
Validation + no partial debit

Round 3
Insufficient funds

Round 4
Same-currency transfer

Round 5
Money rounding

Round 6
Malformed provider responses

Round 7
TTL cache

Round 8
Provider unavailable + stale fallback

Round 9
Idempotency

Round 10
Clock/testability

Round 11
Refactor

Round 12
Production/concurrency discussion
```

Do not read ahead during a timed attempt if you want the closest approximation to pair-programming.
