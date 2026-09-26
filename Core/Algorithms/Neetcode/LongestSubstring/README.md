# Longest Substring Without Repeating/Duplicate Characters

**Difficulty:** Medium
**Company:** Visa
**Topics:** Strings, Sliding Window

---

## Problem Statement

Given a string `s`, find the **length of the longest substring** that contains no duplicate characters.

> A **substring** is a contiguous sequence of characters within a string — not to be confused with a subsequence.

---

## Examples

### Example 1

```text
Input:  s = "abcabcbb"
Output: 3
```

> The longest substring without duplicates is `"abc"`, with length **3**.

### Example 2

```text
Input:  s = "bbbbb"
Output: 1
```

> The longest substring without duplicates is `"b"`, with length **1**.

### Example 3

```text
Input:  s = "pwwkew"
Output: 3
```

> The longest substring without duplicates is `"wke"`, with length **3**.
> Note: `"pwke"` is a subsequence, not a substring — it does not qualify.

---

## Constraints

| Property      | Bound                                        |
| ------------- | -------------------------------------------- |
| String length | `0 <= s.length <= 5 * 10⁴`                   |
| Characters    | English letters, digits, symbols, and spaces |
