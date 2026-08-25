# Minimum Size Subarray Sum

**Difficulty:** Medium
**Topics:** Array, Binary Search, Sliding Window, Prefix Sums

---

## Problem Description

You are given an array of positive integers `nums` and a positive integer `target`.

Return the **minimal length** of a subarray whose sum is **greater than or equal to** `target`. If there is no such subarray, return `0` instead.

A **subarray** is a contiguous non-empty sequence of elements within an array.

---

## Examples

### Example 1

```id="p2x8qa"
Input: target = 10, nums = [2,1,5,1,5,3]
Output: 3
```

**Explanation:**
The subarray `[5,1,5]` has the minimal length under the problem constraint.

---

### Example 2

```id="y7m3zr"
Input: target = 5, nums = [1,2,1]
Output: 0
```

---

## Constraints

- `1 <= nums.length <= 100,000`
- `1 <= nums[i] <= 10,000`
- `1 <= target <= 1,000,000,000`

---

## Follow Up

If you have figured out the `O(n)` solution, try coding another solution with a time complexity of `O(n log n)`.
