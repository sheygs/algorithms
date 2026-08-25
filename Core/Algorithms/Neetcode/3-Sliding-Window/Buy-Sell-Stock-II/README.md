# Best Time to Buy and Sell Stock II

**Difficulty:** Medium
**Topics:** Array, Dynamic Programming, Greedy

---

## Problem Description

You are given an integer array `prices` where `prices[i]` is the price of a given stock on the `i`th day.

On each day, you may decide to buy and/or sell the stock. You can only hold **at most one share** of the stock at any time. However, you can sell and buy the stock multiple times on the same day, ensuring you never hold more than one share.

Return the **maximum profit** you can achieve.

---

## Examples

### Example 1

```text
Input: prices = [7,1,5,3,6,4]
Output: 7
```

**Explanation:**

* Buy on day 2 (price = 1), sell on day 3 (price = 5) → profit = 4
* Buy on day 4 (price = 3), sell on day 5 (price = 6) → profit = 3
* Total profit = 4 + 3 = 7

---

### Example 2

```text
Input: prices = [1,2,3,4,5]
Output: 4
```

**Explanation:**

* Buy on day 1 (price = 1), sell on day 5 (price = 5) → profit = 4
* Total profit = 4

---

### Example 3

```text
Input: prices = [7,6,4,3,1]
Output: 0
```

**Explanation:**
No profitable transactions can be made, so the maximum profit is 0.

---

## Constraints

* `1 <= prices.length <= 3 * 10^4`
* `0 <= prices[i] <= 10^4`
