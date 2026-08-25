# Money Transfer

You are given an existing money-transfer service. Review the code and modify it so a transfer only proceeds when the recipient exists and has an account in the requested target currency.
Avoid leaving the sender partially debited when validation fails.

Treat the exchange-rate provider as supplied code. Don't redesign everything upfront.

## Starter Code

```java
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

    public void send(
            String senderId,
            String recipientId,
            BigDecimal amount,
            String targetCurrency) {

        User sender = userRepository.findById(senderId);
        User recipient = userRepository.findById(recipientId);

        Account senderAccount = sender.getAccount("GBP");

        senderAccount.debit(amount);

        BigDecimal rate =
                exchangeRateProvider.getRate(
                        senderAccount.getCurrency(),
                        targetCurrency
                );

        BigDecimal convertedAmount =
                amount.multiply(rate);

        Account recipientAccount =
                recipient.getAccount(targetCurrency);

        recipientAccount.credit(convertedAmount);
    }

    public BigDecimal receive(
            String userId,
            String currency) {

        User user = userRepository.findById(userId);

        return user.getAccount(currency).getBalance();
    }
}


// ---------- Existing domain code ----------

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

    public Account(String currency, BigDecimal balance) {
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
            String toCurrency
    );
}
```

## Requirements

### Roadmap

| Phase | TODOs | Focus |
| --- | --- | --- |
| Core transfer safety | 1–4 | Validation, side-effect ordering, insufficient funds, same-currency transfers |
| Exchange-rate reliability | 5–6 | TTL cache, provider failure, stale-rate fallback |
| Transfer correctness | 7–10 | Idempotency, money precision, malformed input/rates, deterministic time |
| Later / stretch work | 11–18 | Cross-rates, refactoring, deeper tests, concurrency, retries, cache lifecycle, timed mock |

> Keep the implementation incremental. Do not introduce the later/stretch concerns before the core behaviour is correct and explainable.

### TODO 1 — Fix/Enhance Code

A transfer should happen only if:

1. The sender exists.
2. The recipient exists.
3. The sender has the required source account.
4. The recipient has an account in targetCurrency.
5. The transfer can be completed without partially debiting the sender.

---

### TODO 2 — Handle Exchange-Rate Failure Safely

**New Requirements**

Your job is to update `send()` so that:

1. You obtain the exchange rate before any debit/credit.
2. A null rate is treated as invalid.
3. A provider exception causes the transfer to fail.
4. The sender's balance remains unchanged.
5. The recipient's balance remains unchanged.

---

### TODO 3 — Insufficient Funds

**New requirement:**

The transfer must not proceed if the sender does not have enough money in the source account.

Update `send()` so that:

1. Check the sender account balance is at least amount.
2. If the balance is insufficient, fail immediately.
3. Do not call the exchange-rate provider when you already know the transfer cannot happen.
4. Neither account balance should change.

---

### TODO 4 — Same Currency Transfer

**Requirement**

Update the money transfer flow so that when the sender's source currency and the recipient's target currency are the same, the transfer is completed without calling the exchange-rate provider.

**Expected Behaviour**

Given:

- source currency: `GBP`
- target currency: `GBP`
- amount: `100`

The recipient should receive exactly:

```text
100 GBP
```

The exchange-rate provider must not be called because no currency conversion is required.

**Rules**

1. Preserve all existing input and account validations.
2. Preserve the insufficient-funds check.
3. If sourceCurrency and targetCurrency are equal:
   - do not call `exchangeRateProvider.getRate(...)`;
   - use the original amount as the amount to credit.
4. If the currencies are different:
   - retrieve the exchange rate;
   - validate the returned rate;
   - calculate the converted amount.
5. Debit and credit should still happen only after all required validations and calculations succeed.

**Acceptance Criteria**

1. Same-currency transfers do not call the exchange-rate provider.
2. Same-currency transfers credit exactly the original amount.
3. Different-currency transfers continue to use the exchange-rate provider.
4. Existing validation behaviour remains unchanged.
5. A failed validation must not modify either account balance.

---

### TODO 5 — Cache Exchange Rates

Calling the exchange-rate provider is expensive. Update the transfer service so exchange rates are **cached for a configurable amount of time** and reused. A cached rate may only be used while it is still valid.

**Example**

```text
GBP -> EUR
rate = 1.17
TTL  = 30 seconds
```

If another `GBP -> EUR` transfer happens **before** the cached rate expires, the existing cached rate should be reused and the exchange-rate provider should **not** be called again.

**Cache Entry Structure**

Each cache entry fundamentally needs two pieces of information:

- **exchange rate**
- **expiry time**

**Expected Behaviour**

**1. First Request — No Cached Rate Exists**

```text
sourceCurrency = GBP
targetCurrency = EUR
amount         = 100
```

Expected:

```text
call exchangeRateProvider
validate returned rate
cache the rate
calculate converted amount
complete transfer
```

**2. Request While Cache Is Still Valid**

Another `GBP -> EUR` transfer occurs **before the configured TTL expires**.

Expected:

```text
reuse cached rate
exchange-rate provider calls = 0
complete transfer using cached rate
```

**3. Request After Cache Expiry**

Another `GBP -> EUR` transfer occurs **after the cached rate has expired**.

Expected:

```text
call exchangeRateProvider again
validate returned rate
replace expired cached rate
complete transfer using the new rate
```

**Rules**

1. **Cache exchange rates by currency pair.**

   ```text
   GBP -> EUR
   GBP -> USD
   EUR -> GBP
   ```

   Each pair should have its own cached value.

2. **Each cached entry must contain:**
   - the exchange rate;
   - its expiry time.

3. **A cached rate may only be used while it is still valid.**

4. **If the cached rate has expired:**
   - call the exchange-rate provider;
   - validate the returned rate;
   - replace the expired cached entry.

5. **Do not cache:**
   - a `null` rate;
   - a zero rate;
   - a negative rate;
   - a failed provider response.

6. **The cache TTL must be configurable** and must not be hardcoded inside the transfer logic.

7. **Same-currency transfers must continue to bypass currency conversion entirely.**

   ```text
   GBP -> GBP
   ```

   should:
   - not access the exchange-rate cache;
   - not call the exchange-rate provider;
   - credit the original transfer amount.

8. **Preserve all existing validations:**
   - valid method arguments;
   - sender exists;
   - recipient exists;
   - sender has the source-currency account;
   - recipient has the target-currency account;
   - sender has sufficient funds.

9. **Preserve the existing side-effect rule:**

   ```text
   validations
       ↓
   obtain valid exchange rate
       ↓
   calculate converted amount
       ↓
   debit sender
       ↓
   credit recipient
   ```

   No account balance should change if rate retrieval or validation fails.

**Scope**

For this task, assume the service is **single-threaded**.

**Do NOT implement yet**

- concurrent cache access;
- cache stampede protection;
- Redis or distributed caching;
- stale-rate fallback;
- retries;
- cache size limits or eviction policies;
- multi-instance cache coordination.

**Acceptance Criteria**

- [ ] The first request for a currency pair calls the exchange-rate provider.
- [ ] A fresh cached rate is reused without calling the provider.
- [ ] An expired cached rate causes the provider to be called again.
- [ ] The refreshed rate replaces the expired cached entry.
- [ ] Different currency pairs are cached independently.
- [ ] Invalid rates are never cached.
- [ ] Provider failures do not update the cache.
- [ ] Same-currency transfers bypass both the cache and provider.
- [ ] Existing transfer validation behaviour remains unchanged.
- [ ] Failed rate retrieval must not modify either account balance.

---

### TODO 6 — Provider Unavailable + Stale Cached Rate

New requirement:

> If the cached exchange rate has expired and the exchange-rate provider fails, decide whether the transfer may use the expired cached rate.

For this exercise, use this policy:

> **Allow the stale cached rate as a fallback only when the provider fails and an expired cached rate exists.**

**Requirements:**

1. Fresh cached rate → use it; do not call provider.
2. No cached rate → call provider.
3. Expired cached rate → try the provider first.
4. Provider succeeds → validate and cache the new rate.
5. Provider fails + expired cached rate exists → use the stale rate.
6. Provider fails + no cached rate exists → fail the transfer.
7. Invalid provider rates must still never be cached.
8. No debit/credit should happen until a usable rate has been determined.

Don't implement retries or idempotency yet.

The key thing to think about first is:

> **How do you distinguish "expired cache exists" from "no cache exists" while still attempting the provider in both cases?**

---

### TODO 7 — Idempotent Money Transfer with Original Result Replay

> Make transfers idempotent and return the original transfer result for duplicate requests.

Update the transfer service so a client can safely retry the same transfer request without causing duplicate side effects (double debit/credit, or a second call to the exchange-rate provider).

- Each logical transfer must have a unique `transferId`.
- If a transfer with the same `transferId` has already completed successfully, the service must return the original result without re-performing the transfer.

**Method Change**

Update `send(...)` so that it returns a `TransferResult` and accepts a `transferId`.

```java
TransferResult send(
    String transferId,
    String senderId,
    String recipientId,
    BigDecimal amount,
    String sourceCurrency,
    String targetCurrency
)
```

**TransferResult**

A result object containing the useful details of the completed transfer.

```java
record TransferResult(
    String transferId,
    TransferStatus status,
    BigDecimal sentAmount,
    String sourceCurrency,
    BigDecimal receivedAmount,
    String targetCurrency,
    BigDecimal exchangeRate
) {}
```

For this exercise:

```java
enum TransferStatus {
    SUCCESS
}
```

For a same-currency transfer, use `BigDecimal.ONE` as the exchange rate.

**Idempotency Storage**

Need to remember more than whether a transfer completed — need to remember the **original result** so it can be returned on retry.

```java
Map<String, TransferResult>
```

**Requirements**

1. `send()` receives a unique `transferId`.
2. A new `transferId` is processed normally.
3. A successfully completed transfer produces a `TransferResult`.
4. Store that result against its `transferId`.
5. If the same transfer is submitted again with the same `transferId`, return the original `TransferResult` without performing the transfer again.
6. Do not debit, credit, or call the exchange-rate provider again for a completed duplicate.
7. If a request fails **before successfully completing the transfer**, don't record it as completed; allow the caller to retry.
   - _Simplified exercise policy_ — real APIs can have more nuanced failure caching. Stripe, for instance, persists some execution results including server errors, while requests rejected before endpoint execution can be retried. (Stripe Docs)
8. Reusing a `transferId` for materially different transfer details should be considered invalid.
9. Keep the implementation **single-threaded** for this TODO; concurrency comes afterward.

**Acceptance Criteria**

- [ ] A new `transferId` is processed normally.
- [ ] A successful transfer returns a `TransferResult`.
- [ ] A successful result is stored against its `transferId`.
- [ ] Repeating a completed `transferId` returns the original stored `TransferResult`.
- [ ] A duplicate request does not debit the sender again.
- [ ] A duplicate request does not credit the recipient again.
- [ ] A duplicate request does not unnecessarily call the exchange-rate provider.
- [ ] A failed transfer is not marked completed.
- [ ] A failed transfer may be retried with the same `transferId`.
- [ ] Same-currency transfers return `exchangeRate = BigDecimal.ONE`.
- [ ] Different-currency transfers return the actual rate used.
- [ ] Existing validation, cache, stale fallback, and side-effect ordering continue to work.
- [ ] Keep this TODO single-threaded.

**Main Design Question**

Before coding, be able to answer — at what point do I:

1. check for an existing transfer result?
2. perform the money movement?
3. create the `TransferResult`?
4. store the `TransferResult`?
5. return it?

**Central invariant:**

> A `transferId` is only considered completed after the transfer's side effects have succeeded.

---

### TODO 8 — Money Rounding and Currency Precision

**Requirement**

Update the transfer flow so that the final amount credited to the recipient respects the precision of the target currency.

> **Do not round the exchange rate itself.** Use the full exchange-rate precision for the calculation, then round the resulting monetary amount.

**Expected Behaviour**

**Different-currency transfer**

```text
amount         = 100 GBP
exchangeRate   = 1.1734567
targetCurrency = EUR
```

Raw converted amount:

```text
117.3456700 EUR
```

EUR uses 2 fractional digits, so the credited amount should be:

```text
117.35 EUR
```

**Zero-decimal currency (e.g. JPY)**

```text
raw amount = 19123.72 JPY
```

Final credited amount, rounded using the chosen rounding policy:

```text
19124 JPY
```

**Rules**

1. Keep the exchange rate at its original precision.
2. Calculate the raw converted amount using:
   ```text
   amount × exchangeRate
   ```
3. Determine the number of fractional digits supported by the target currency.
4. Round the final converted amount to that scale.
5. Use an explicit rounding policy.
6. For this practice exercise, use:
   ```java
   RoundingMode.HALF_EVEN
   ```
7. Apply the target-currency precision **before** modifying either account balance.
8. The rounded amount is:
   - credited to the recipient
   - stored as `receivedAmount` in the `TransferResult`
9. Same-currency transfers must also respect the target currency's precision.
10. Do not round the cached exchange rate.

**Suggested Java APIs**

```java
Currency.getInstance(targetCurrency)
        .getDefaultFractionDigits();
```

```java
convertedAmount.setScale(
        scale,
        RoundingMode.HALF_EVEN
);
```

**Required Flow**

```text
validate transfer
        ↓
resolve exchange rate
        ↓
calculate raw converted amount
        ↓
determine target currency scale
        ↓
round converted amount
        ↓
debit sender
        ↓
credit recipient
        ↓
create TransferResult
```

**Same-Currency Example**

```text
sourceCurrency = GBP
targetCurrency = GBP
amount         = 100
exchangeRate   = 1
```

Expected:

```text
sentAmount     = 100
receivedAmount = 100.00
exchangeRate   = 1
```

> The exchange-rate provider must still **not** be called.

**Acceptance Criteria**

- [ ] The exchange rate itself is not rounded.
- [ ] The converted amount is rounded only after multiplication.
- [ ] The target currency determines the number of fractional digits.
- [ ] `RoundingMode.HALF_EVEN` is used.
- [ ] EUR/GBP-style currencies are represented using their supported fractional precision.
- [ ] Zero-decimal currencies are rounded appropriately.
- [ ] Same-currency transfers also use the target currency's precision.
- [ ] The recipient is credited with the rounded amount.
- [ ] `TransferResult.receivedAmount` contains the rounded amount.
- [ ] No debit or credit occurs before rounding has completed successfully.

**Out of Scope**

Do **not** add:

- Custom currency metadata
- Configurable rounding policies per currency
- Provider-rate rounding
- Fee calculations
- FX spreads
- Tax calculations
- Currency conversion libraries beyond what is required for this exercise

---

### TODO 9 — Unsupported Currency & Malformed Exchange-Rate Handling

**Overview**

The transfer flow must fail **safely** whenever it encounters:

1. An unsupported source currency
2. An unsupported target currency
3. An unusable (malformed) rate from the exchange-rate provider

In every one of these cases, **no account balance may change** — no debit, no credit, and no bad data written to the cache.

**1. Currency Validation**

Every currency supplied to the transfer must be checked against a set of supported currencies **before** any account mutation happens.

| Currency | Status         |
| -------- | -------------- |
| GBP      | ✅ Valid       |
| EUR      | ✅ Valid       |
| JPY      | ✅ Valid       |
| ABC      | ❌ Unsupported |
| XYZ      | ❌ Unsupported |

**Rule:** if either `sourceCurrency` or `targetCurrency` is unsupported, the transfer is rejected immediately.

**Example**

```text
sourceCurrency = "ABC"
targetCurrency = "EUR"
```

Expected outcome:

- Transfer fails
- Exchange-rate provider is **not** called
- Sender is **not** debited
- Recipient is **not** credited

The same applies if `targetCurrency` is the unsupported one.

**2. Malformed Provider Rate Validation**

A rate returned by the provider is only usable if:

```text
rate != null AND rate > 0
```

Any of the following is considered **malformed** and must be rejected:

| Rate value | Status     |
| ---------- | ---------- |
| `null`     | ❌ Invalid |
| `0`        | ❌ Invalid |
| `-1.25`    | ❌ Invalid |

If the rate fails this check:

- The rate-resolution path fails
- The rate is **never cached**
- The sender is **not** debited
- The recipient is **not** credited

**3. Provider Unavailable vs. Provider Returned Bad Data**

These are two distinct failure modes and must be handled differently:

| Failure Mode                       | Meaning                                        | Example                        |
| ---------------------------------- | ---------------------------------------------- | ------------------------------ |
| **Provider unavailable**           | The call to the provider itself fails          | `ExchangeRateException` thrown |
| **Provider returned invalid data** | The call succeeds, but the value can't be used | `rate = null / 0 / -1`         |

Both must prevent an unsafe transfer, but only _provider unavailable_ interacts with the stale-cache fallback described below. A malformed response is rejected outright at the validation step, regardless of any stale cache.

**4. Cache Behaviour (existing rules — must still hold)**

| Scenario                                                | Behaviour                                                          |
| ------------------------------------------------------- | ------------------------------------------------------------------ |
| Fresh cached rate exists                                | Use cached rate. Provider is **not** called.                       |
| Cache missing / expired                                 | Call provider. If the returned rate is valid, cache it and use it. |
| Provider failure (exception) + stale cached rate exists | Fall back to the stale cached rate.                                |
| Provider failure (exception) + no stale cached rate     | Transfer fails.                                                    |

**5. Same-Currency Behaviour (unchanged)**

```text
sourceCurrency = "GBP"
targetCurrency = "GBP"
```

Expected:

- `exchangeRate = 1`
- Exchange-rate provider is **not** called

Note: the currency itself must still pass the supported-currency check even in the same-currency case.

**6. Required Flow**

```text
validate transferId / basic arguments
            │
validate source currency
            │
validate target currency
            │
check idempotency result
            │
validate sender / recipient / accounts / funds
            │
        same currency?
        /            \
      yes              no
       │                │
   rate = 1        resolve rate
       │                │
       │         validate provider result
       │                │
       │         cache only valid rate
        \              /
         \            /
          calculate amount
                │
          apply rounding
                │
              debit
                │
              credit
```

**Acceptance Criteria**

- [ ] Unsupported `sourceCurrency` is rejected
- [ ] Unsupported `targetCurrency` is rejected
- [ ] Unsupported currencies do not cause account mutations
- [ ] Invalid provider rates are rejected
- [ ] `null` rates are rejected
- [ ] Zero rates are rejected
- [ ] Negative rates are rejected
- [ ] Invalid provider rates are never cached
- [ ] A failed rate-resolution path does not debit the sender
- [ ] A failed rate-resolution path does not credit the recipient
- [ ] Existing fresh-cache behaviour still works
- [ ] Existing stale-cache fallback behaviour still works
- [ ] Existing same-currency behaviour still works
- [ ] Existing idempotency behaviour still works

**Out of Scope**

The following are explicitly **not** part of this TODO:

- Cross-rate conversion
- Arbitrary currency graphs
- Retries
- Multiple providers
- Configurable fallback chains
- Distributed caching
- Concurrency handling
- Database persistence

---

### TODO 10 — Inject `Clock` for Deterministic Cache Expiry

**Summary**

`TransferService` currently reads the system clock directly via `Instant.now()` when checking or setting exchange-rate cache expiry. This makes TTL behaviour non-deterministic and forces tests to rely on real wall-clock delays (e.g. `Thread.sleep()`).

This change introduces an injected `Clock` dependency so that "now" is fully controllable in tests, with no change to production behaviour.

**Why**

- TTL tests currently require waiting for real time to pass.
- `Instant.now()` calls are hidden, hard-to-mock dependencies buried in business logic.
- A `Clock` seam lets tests use `Clock.fixed(...)` to assert exact behaviour at exact instants — including the expiry boundary — with zero flakiness and zero delay.

**Changes**

**1. Add `Clock` as a constructor dependency**

```java
private final Clock clock;

TransferService(
    UserRepository userRepository,
    ExchangeRateProvider exchangeRateProvider,
    Duration cacheTTL,
    Clock clock
)
```

The clock is never constructed inside business logic — it is always passed in.

**2. Replace all direct time access**

Every occurrence of:

```java
Instant.now()
```

inside `TransferService` becomes:

```java
Instant.now(clock)
```

This applies to both:

- checking whether a cached rate has expired
- calculating the expiry timestamp of a newly cached rate

**3. New cache entries**

```java
Instant expiresAt = Instant.now(clock).plus(cacheTTL);
```

**Behaviour**

**Freshness rule (unchanged, now clock-driven)**

| now vs expiresAt   | Result                          |
| ------------------ | ------------------------------- |
| `now < expiresAt`  | fresh — cached rate is used     |
| `now >= expiresAt` | expired — provider is consulted |

**Example — fresh:**
`expiresAt = 10:00:30`, `now = 10:00:29` → cached rate used, provider **not** called.

**Example — exact boundary:**
`expiresAt = 10:00:30`, `now = 10:00:30` → treated as **expired** (boundary is inclusive of expiry).

**Example — expired:**
`expiresAt = 10:00:30`, `now = 10:00:31` → provider is consulted.

**Fallback chain on expiry (unchanged)**

```text
provider succeeds        → validate → cache new rate → use it
provider fails + stale cache exists → use stale rate
provider fails + no cache exists    → fail transfer
```

**Usage**

**Production:**

```java
new TransferService(
    userRepository,
    exchangeRateProvider,
    Duration.ofSeconds(30),
    Clock.systemUTC()
);
```

**Tests:**

```java
Clock fixedClock = Clock.fixed(
    Instant.parse("2024-01-01T10:00:30Z"),
    ZoneOffset.UTC
);

TransferService service = new TransferService(
    userRepository,
    exchangeRateProvider,
    Duration.ofSeconds(30),
    fixedClock
);
```

No `Thread.sleep()` required — time is set explicitly per test case.

**Out of Scope**

Writing the actual TTL test suite is **not** part of this change; only the `Clock` seam itself.

**Non-Goals / Must Not Change**

Adding `Clock` must not alter any of the following existing behaviour:

- argument validation
- unsupported-currency validation
- sender/recipient validation
- account validation
- insufficient-funds validation
- same-currency transfers
- exchange-rate caching logic
- malformed provider-response handling
- stale-cache fallback
- money rounding
- idempotency
- side-effect ordering

**Acceptance Criteria**

- [ ] `TransferService` receives a `Clock` through its constructor
- [ ] No direct `Instant.now()` calls remain in transfer/cache logic
- [ ] Cache freshness check uses `Instant.now(clock)`
- [ ] New cache expiry uses `Instant.now(clock).plus(cacheTTL)`
- [ ] A cache entry is fresh strictly before `expiresAt`
- [ ] A cache entry is expired exactly at `expiresAt`
- [ ] Tests can control time without `Thread.sleep()`
- [ ] All existing transfer behaviour is unchanged

**Constructor configuration validation**

The service should also reject invalid construction-time dependencies/configuration:

- `userRepository` must not be `null`
- `exchangeRateProvider` must not be `null`
- `cacheTTL` must not be `null`
- `cacheTTL` must be greater than zero
- `clock` must not be `null`

This keeps `TransferService` from existing in an invalid configuration.

---

## Later / Stretch TODOs

These are useful follow-ups after the core transfer flow is working. They should not be allowed to obscure the main interview goals: safe validation, correct money movement, clear side-effect ordering, rate handling, idempotency, and explainable trade-offs.

### TODO 11 — Missing Direct Rate / Cross-Rate

**New Requirement**

The exchange-rate provider may not always support a direct currency pair.

**Example**

```text
Requested:
GBP → JPY

Direct rate:
GBP → JPY = unavailable

But provider supports:
GBP → USD
USD → JPY
```

For this exercise, assume **USD** is the configured base currency.

**Task**

Support:

```text
GBP → JPY
```

by obtaining:

```text
GBP → USD
USD → JPY
```

and deriving the effective rate.

**Keep All Existing Behaviour Intact**

- Fresh cache first
- TTL expiry
- Stale-rate fallback
- Rate validation
- Money rounding
- Same-currency bypass
- Idempotency
- No account side effects until a usable rate has been determined

**Constraint**

Do not implement an arbitrary currency graph/BFS. Only support one configured intermediate/base currency for this TODO.

**Question to Reason About Before Coding**

How will your code know that the direct rate is unavailable, so that it should attempt the cross-rate path instead?


---

### TODO 12 — Extract Exchange-Rate Resolution from `send()`

**Goal**

Keep `send()` focused on the transfer business flow by extracting cache/provider/stale-fallback logic into a dedicated helper such as:

```java
private BigDecimal resolveRate(
        String sourceCurrency,
        String targetCurrency) {
    ...
}
```

**Responsibilities of `resolveRate(...)`**

1. Build the `CurrencyPair`.
2. Look up the cached rate.
3. Return a fresh cached rate immediately.
4. On cache miss/expiry, call the provider.
5. Validate the provider response.
6. Cache a valid fresh rate with a new expiry.
7. If the provider throws and a stale cached rate exists, return the stale rate.
8. If no usable rate exists, fail.

**What stays in `send()`**

```text
validate request
    ↓
load users/accounts
    ↓
check funds
    ↓
resolve rate
    ↓
convert + round
    ↓
debit
    ↓
credit
    ↓
create/store TransferResult
    ↓
return
```

**Constraint**

This is a refactor, not a behaviour change. Existing cache, stale fallback, validation, rounding, idempotency, and side-effect rules must remain unchanged.

---

### TODO 13 — Focused Regression Test Suite

**Goal**

Verify the behaviour that is easiest to break while refactoring.

**High-value tests**

- fresh cache hit reuses the rate and does not call the provider again;
- exact TTL boundary (`now == expiresAt`) is treated as expired;
- provider failure + stale cache uses the stale rate;
- provider failure + no stale cache fails with balances unchanged;
- same-currency transfer does not call the provider;
- malformed provider rates (`null`, zero, negative) do not change balances;
- unsupported currencies fail before provider/account side effects;
- insufficient funds fails before the provider call;
- EUR/GBP-style currencies round to their supported precision;
- JPY-style zero-decimal currencies round correctly;
- duplicate `transferId` returns the original result and does not debit/credit again.

**Clock strategy**

Use an injected test clock (for example a small mutable `Clock`) rather than `Thread.sleep()` so TTL tests remain deterministic.

---

### TODO 14 — Idempotency Request-Fingerprint Mismatch

The current exercise replays a result solely by `transferId`.

A stronger implementation should detect this case:

```text
first request:
transferId = TX-100
Alice → Bob
100 GBP → EUR

later request:
transferId = TX-100
Alice → Bob
500 GBP → EUR
```

The second request must not silently receive the original result as though the requests were identical.

**Production-oriented approach**

Persist the idempotency key together with a request fingerprint containing the material transfer fields, for example:

```text
senderId
recipientId
amount
sourceCurrency
targetCurrency
```

Then:

```text
same transferId + same fingerprint
    → replay original result

same transferId + different fingerprint
    → reject as an idempotency conflict
```

Keep this separate from the basic single-threaded idempotency exercise.

---

### TODO 15 — Concurrent Cache Access and Cache Stampede

The original cache TODO intentionally assumed a single-threaded service.

In a real multi-threaded service, two requests can miss or expire the same currency pair at the same time:

```text
Thread A: GBP/EUR cache miss → provider call
Thread B: GBP/EUR cache miss → provider call
Thread C: GBP/EUR cache miss → provider call
```

This can cause a **cache stampede** against the external provider.

**Later design questions**

- Use `ConcurrentHashMap` for safe local concurrent access.
- Decide whether duplicate provider calls for the same pair are acceptable.
- If not, consider per-key locking or request coalescing so one thread refreshes a pair while others reuse/wait for that result.
- In a multi-instance system, decide whether coordination belongs in a distributed cache or whether occasional duplicate refreshes are acceptable.

Do not confuse this with transfer idempotency: cache coordination protects provider load; transfer idempotency protects the money-movement invariant.

---

### TODO 16 — Provider Retry Policy and Resilience

Add retries only after failure modes are clearly classified.

**Retry candidates**

Transient provider failures such as:

```text
timeout
temporary network error
HTTP 5xx / provider unavailable
```

**Do not retry blindly**

Do not retry:

```text
null/zero/negative rate
unsupported currency
invalid request
```

Those are not transient failures.

**Design points**

- bounded retry count;
- timeout per attempt;
- backoff/jitter;
- stale-rate fallback policy;
- metrics/logging for provider failures and stale-rate usage.

The rate lookup occurs before account side effects, so retrying a read-only provider lookup is fundamentally different from retrying the money movement itself.

---

### TODO 17 — Cache Size and Eviction Policy

The current map can grow as more currency pairs are cached.

Questions to reason about:

- Is the number of supported currency pairs naturally bounded?
- Should expired entries be removed eagerly or lazily?
- Should the cache have a maximum size?
- Would a dedicated cache library or Redis be more appropriate in production?

For this exercise, avoid adding eviction complexity unless the requirement explicitly asks for it.

---

### TODO 18 — Clean-Starter Timed Mock

After completing the rough-code/refactoring version, practise the same domain from a cleaner starter skeleton under interview time pressure.

**Goal**

Be able to implement the core flow without relying on the evolved solution:

```text
understand supplied interfaces
    ↓
validate inputs/entities/accounts
    ↓
resolve rate
    ↓
round money
    ↓
perform side effects safely
    ↓
return result
```

**Suggested constraint**

Run it as a 45–60 minute mock. Prioritise correctness and communication over implementing every stretch feature.

---

## Production Review — Current Limitations & Solutions

### Issue 1: Debit and credit are not atomic

The current implementation performs the sender debit and recipient credit as two separate operations.

If the debit succeeds but the credit fails, the sender could lose money without the recipient receiving it.

**Solution 1: Use a database transaction**

Perform both balance updates within the same transactional boundary.

If either operation fails, roll back the entire transaction so that either both balance changes succeed or neither does.

---

### Issue 2: Idempotency is vulnerable to concurrent requests

The current implementation checks `completedTransfers` first and stores the result only after the transfer succeeds.

Two concurrent requests with the same `transferId` could both observe that the transfer does not yet exist and both execute the transfer.

Simply replacing `HashMap` with `ConcurrentHashMap` would make individual map operations thread-safe, but it would not make the complete check-process-store operation atomic.

**Solution 2: Use durable idempotency with a unique transfer ID**

Persist the transfer/idempotency record in a shared database and enforce a unique constraint on `transferId`.

Only one request should be able to claim and process a particular transfer ID.

Other requests with the same ID should return the already-recorded result rather than execute the money movement again.

---

### Issue 3: Idempotency state exists only in application memory

`completedTransfers` is currently stored inside the service instance.

If the application restarts, that state disappears.

It is also not shared between multiple application instances.

For example, request 1 could reach server A and request 2 with the same `transferId` could reach server B. Each server would have a different in-memory map.

**Solution 3: Persist transfer/idempotency state in a shared durable store**

Store completed transfers and idempotency records in a database such as PostgreSQL.

This provides durability across restarts and allows all service instances to use the same source of truth.

---

### Issue 4: Exchange-rate cache is local to one application instance

The current `ratesCache` is stored in memory.

If several application instances are running, each instance maintains its own cache and may independently call the external exchange-rate provider.

The cache is also lost whenever an instance restarts.

**Solution 4: Use an appropriate shared cache when necessary**

A distributed cache such as Redis could be used for exchange rates so multiple service instances can reuse the same cached values.

Because exchange rates are cache data rather than the durable source of truth, losing the cache should affect performance or availability rather than financial correctness.

---

### Issue 5: The current HashMaps are not thread-safe

`HashMap` is not intended for concurrent modification by multiple threads.

A production service may process many transfers simultaneously.

**Solution 5: Use concurrency-safe data structures where local shared state remains**

For local in-memory state, use concurrency-safe structures such as `ConcurrentHashMap`.

However, thread-safe data structures alone do not solve higher-level races such as idempotency. Business operations that must happen once still require atomic coordination, usually through the database or another shared coordination mechanism.

---

### Interview Summary

The current implementation is suitable for demonstrating the business logic, but in production I would change three important things:

1. Put debit and credit inside a transactional boundary.
2. Persist idempotency records with a unique `transferId` so duplicate concurrent requests cannot move money twice.
3. Keep durable financial state in the database, while using a shared cache such as Redis only for data that is genuinely cacheable, such as exchange rates.

The key distinction is that thread safety, atomicity, durability, and distributed consistency are separate concerns. `ConcurrentHashMap` helps with thread safety inside one JVM, but it does not by itself provide transaction atomicity, durable idempotency, or coordination across multiple service instances.
