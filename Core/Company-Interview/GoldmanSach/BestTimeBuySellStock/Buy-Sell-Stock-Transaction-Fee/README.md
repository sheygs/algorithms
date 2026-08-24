# Best Time to Buy and Sell Stock with Transaction Fee

## Difficulty

Medium

## Problem Statement

You are given an array `prices` where `prices[i]` is the price of a given stock on the `i`-th
day, and an integer `fee` representing a transaction fee.

Find the maximum profit you can achieve. You may complete as **many transactions** as you like,
but you need to pay the transaction fee for **each transaction**.

**Note:**

- You may not engage in multiple transactions simultaneously (i.e., you must sell the stock
  before you buy again).
- The transaction fee is only charged once for each stock purchase and sale.

---

## Examples

**Example 1:**

```py
Input: prices = [1,3,2,8,4,9], fee = 2
Output: 8
Explanation: The maximum profit can be achieved by:
             - Buying  on day 1 (price = 1) and selling on day 4 (price = 8),
               profit = 8 - 1 - 2 = 5.
             - Buying  on day 5 (price = 4) and selling on day 6 (price = 9),
               profit = 9 - 4 - 2 = 3.
             Total profit = 5 + 3 = 8.
```

**Example 2:**

```py
Input: prices = [1,3,7,5,10,3], fee = 3
Output: 6
```

---

## Constraints

- `1 <= prices.length <= 5 * 10^4`
- `1 <= prices[i] <= 10^5`
- `0 <= fee <= 10^5`

---

## Topics

`Array` `Dynamic Programming` `Greedy`

---

## DP States

```text

hold = -prices[0]   # max cash if you ARE holding a stock
free = 0            # max cash if you are NOT holding a stock


`hold` starts negative because buying costs money. `free` starts at 0 — you begin with no stock and no profit.

---

## The Transitions

free  = max(free,  hold + prices[i] - fee)   # stay free  OR  sell today
hold  = max(hold, free  - prices[i])          # stay holding OR  buy today
```

## Full Walkthrough — `prices=[1,3,2,8,4,9], fee=2`

```text
Start:  hold=-1  free=0

Day 1  price=3:
  free = max(0,  -1+3-2) = max(0, 0)  = 0    (selling gives 0, not worth it yet)
  hold = max(-1,  0-3)   = max(-1,-3) = -1   (stay holding, don't rebuy at 3)

Day 2  price=2:
  free = max(0,  -1+2-2) = max(0,-1)  = 0    (selling at 2 would lose money)
  hold = max(-1,  0-2)   = max(-1,-2) = -1   (stay holding, don't rebuy at 2)

Day 3  price=8:
  free = max(0,  -1+8-2) = max(0, 5)  = 5    (sell! profit = 8-1-2 = 5) ✅
  hold = max(-1,  0-8)   = max(-1,-8) = -1   (don't rebuy at 8)

Day 4  price=4:
  free = max(5,  -1+4-2) = max(5, 1)  = 5    (stay free, selling now gives less)
  hold = max(-1,  5-4)   = max(-1, 1) = 1    (buy at 4! hold=5-4=1) ✅

Day 5  price=9:
  free = max(5,   1+9-2) = max(5, 8)  = 8    (sell! profit = 9-4-2 = 3, total = 8) ✅
  hold = max(1,   5-9)   = max(1,-4)  = 1    (don't rebuy)

Answer: free = 8 ✅
```

### Why `free` is computed before `hold`?

```text
# CORRECT — use prev_hold (yesterday's state) when computing free
free = max(prev_free, prev_hold + prices[i] - fee)
hold = max(prev_hold, prev_free - prices[i])

# WRONG — if you update hold first, free might use today's hold
# this would allow buying AND selling on the same day
hold = max(hold, free - prices[i])    # hold now reflects today
free = max(free, hold + prices[i] - fee)  # uses today's hold — BUG


The snapshot (`prev_hold`, `prev_free`) or the correct ordering guarantees both transitions use yesterday's state — you can't buy and sell on the same day.

---

## The Fee's Role

Without a fee, you'd trade every small uptick. The fee forces you to only trade when the gain is worth the cost:

prices = [1, 2, 3],  fee = 1

Without fee:  buy@1 sell@2 (+1), buy@2 sell@3 (+1) = profit 2
With fee=1:   buy@1 sell@2 (+1-1=0), skip  → better to just hold
              buy@1 sell@3 (+2-1=1) = profit 1
```
