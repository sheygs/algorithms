# String Compression

**Difficulty:** Medium
**Topics:** Two Pointer, Strings, Arrays

---

## Problem Description

You are given an array of characters `chars`. Compress it using the following algorithm:

- Begin with an empty string `s`.
- For each group of **consecutive repeating characters** in `chars`:
  - If the group's length is `1`, append the character to `s`.
  - Otherwise, append the character followed by the group's length.

The compressed string `s` should **not** be returned separately, but instead be stored in the input character array `chars`.

> Note: Group lengths that are `10` or longer will be split into multiple characters in `chars`.
> For example, `10` is represented as `["1","0"]`.

Let `k` be the length of the compressed string `s`. You must modify the **first `k` characters** of the `chars` array and return `k`.

You must write an algorithm that uses **only constant extra space**.

---

## Examples

### Example 1

```id="c1lq8f"
Input: chars = ["a","a","a","a","a","a","a","a","a","a","a"]
Output: 3
```

**Explanation:**
The compressed string is `"a11"` and the first 3 characters of the input array should be `["a","1","1"]`.

---

### Example 2

```id="g8a2nm"
Input: chars = ["A"]
Output: 1
```

**Explanation:**
The compressed string is `"A"` and the first 1 character of the input array should be `["A"]`.

---

### Example 3

```id="h2k9sd"
Input: chars = ["1","1","2"]
Output: 3
```

**Explanation:**
The compressed string is `"122"` and the first 3 characters of the input array should be `["1","2","2"]`.

---

## Constraints

- `1 <= chars.length <= 2000`
- `chars[i]` is a lowercase English letter, uppercase English letter, digit, or symbol.
