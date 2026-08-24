# Two Integer Sum II

> Array, Two Pointer, Binary Search

![Difficulty](https://img.shields.io/badge/Difficulty-Medium-orange) ![Space Complexity](<https://img.shields.io/badge/Space-O(1)-blue>)

## Problem Statement

Given a **sorted (non-decreasing)** array of integers `numbers`, return the **1-indexed** positions of two numbers that add up to a given `target`.

Return the result as `[index1, index2]` where `index1 < index2`. The same element cannot be used twice, and there is always exactly one valid solution.

---

## Examples

### Example 1

```text
Input:  numbers = [1, 2, 3, 4], target = 3
Output: [1, 2]
```

> `numbers[0] + numbers[1]` → `1 + 2 = 3` ✅

---

## Constraints

| Property       | Bound                         |
| -------------- | ----------------------------- |
| Array length   | `2 <= numbers.length <= 1000` |
| Element values | `-1000 <= numbers[i] <= 1000` |
| Target value   | `-1000 <= target <= 1000`     |

---

## Requirements

- **Space Complexity:** O(1) — no additional data structures allowed
- **Indexing:** Return 1-based indices (not 0-based)
- **Guarantee:** Exactly one valid solution always exists
