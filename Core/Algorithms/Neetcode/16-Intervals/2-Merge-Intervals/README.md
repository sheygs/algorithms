# Merge Intervals

**Difficulty:** Medium

---

## Topics

Array, Sorting

---

## Problem Statement

Given an array of `intervals` where `intervals[i] = [start_i, end_i]`, merge all overlapping intervals, and return an array of the non-overlapping intervals that cover all the intervals in the input.

You may return the answer in any order.

> **Note:** Intervals are non-overlapping if they have no common point. For example, `[1, 2]` and `[3, 4]` are non-overlapping, but `[1, 2]` and `[2, 3]` are overlapping.

---

## Examples

**Example 1:**

```text
Input:  intervals = [[1,3],[1,5],[6,7]]
Output: [[1,5],[6,7]]
```

---

**Example 2:**

```text
Input:  intervals = [[1,2],[2,3]]
Output: [[1,3]]
```

---

## Constraints

- `1 <= intervals.length <= 1000`
- `intervals[i].length == 2`
- `0 <= start <= end <= 1000`
