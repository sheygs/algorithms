# Median of Two Sorted Arrays

**Difficulty:** Hard
**Topics:** Array, Binary Search, Divide and Conquer

## Problem Statement

You are given two integer arrays `nums1` and `nums2` of size `m` and `n` respectively, where each array is sorted in **ascending order**.

Return the **median** of all elements across both arrays.

Your algorithm must run in **O(log(m + n))** time.

---

## Definition

The **median** is the middle value of a sorted list.

- If the total number of elements is **odd**, the median is the middle element.
- If the total number of elements is **even**, the median is the **average of the two middle elements**.

---

## Examples

### Example 1

#### Input 1

```text
nums1 = [1,2]
nums2 = [3]
```

#### Output 1

```text
2.0
```

#### Explanation 1

Combined array: `[1,2,3]`
The median is `2`.

---

### Example 2

#### Input 2

```text
nums1 = [1,3]
nums2 = [2,4]
```

#### Output 2

```text
2.5
```

#### Explanation 2

Combined array: `[1,2,3,4]`
The median is `(2 + 3) / 2 = 2.5`.

---

## Constraints

- `nums1.length == m`
- `nums2.length == n`
- `0 <= m <= 1000`
- `0 <= n <= 1000`
- `1 <= m + n <= 2000`
- `-10^6 <= nums1[i], nums2[i] <= 10^6`

---

## Expected Complexity

| Metric           | Requirement         |
| ---------------- | ------------------- |
| Time Complexity  | `O(log(min(m, n)))` |
| Space Complexity | `O(1)`              |

---

## Core Idea

Instead of merging both arrays `(O(n+m))`, you use `binary search` on the smaller array to find the correct partition point that splits the combined array into two equal halves — left and right — such that every element on the left is ≤ every element on the right. The median is then derived from the boundary values.

## Visual Walkthrough

`nums1 = [1, 3]`, `nums2 = [2, 4, 5, 6]`, expected median = `3.5`

```text
A = [1, 3]       len=2
B = [2, 4, 5, 6] len=4
total = 6,  half = 3
l=0, r=1
```

**Iteration 1:** `l=0, r=1`

```text
i = (0+1)//2 = 0      → A's left half = [1]
j = 3 - 0 - 2 = 1     → B's left half = [2, 4]

Aleft =1,  Aright=3
Bleft =4,  Bright=5

Aleft(1) <= Bright(5)? ✅
Bleft(4) <= Aright(3)? ❌  → Bleft too large

A's partition too far LEFT → l = i + 1 = 1
```

**Iteration 2:** `l=1, r=1`

```text
i = (1+1)//2 = 1      → A's left half = [1, 3]
j = 3 - 1 - 2 = 0     → B's left half = [2]

Aleft =3,  Aright=inf
Bleft =2,  Bright=4

Aleft(3) <= Bright(4)? ✅
Bleft(2) <= Aright(inf)? ✅  → correct partition!

total is EVEN:
  max(Aleft, Bleft)  = max(3, 2) = 3   ← largest left boundary
  min(Aright, Bright) = min(inf, 4) = 4 ← smallest right boundary

median = (3 + 4) / 2 = 3.5 ✅
```

---

## Why the `-2` in `j = half - i - 2`

```text
i is 0-based → A contributes (i + 1) elements to the left half
j is 0-based → B contributes (j + 1) elements to the left half

Total left elements must equal half:
  (i + 1) + (j + 1) = half
   j + 1  = half - i - 1
   j      = half - i - 2
```

---

## Why Sentinels (`±infinity`)?

When a partition sits at the very edge, one side has no elements. Sentinels handle this cleanly:

```text
i = -1 → A contributes nothing to left  → Aleft = -inf
           (any Bleft will be ≥ -inf, so this side never blocks)

i+1 = len(A) → A contributes nothing to right → Aright = +inf
               (any Bright will be ≤ +inf, so this side never blocks)
```
