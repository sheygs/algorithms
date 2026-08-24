# Valid Anagram

## 📌 Problem Description

Given two strings `s` and `t`, return **`true`** if the two strings are anagrams of each other, otherwise return **`false`**.

An **anagram** is a string that contains the exact same characters as another string, but the order of the characters can be different.

---

## 🧠 Topics

- Strings
- Hash Tables
- Sorting

---

## 📝 Examples

### Example 1

```text
Input: s = "racecar", t = "carrace"
Output: true
Explanation: Both strings contain the same characters with the same frequency.
```

### Example 2

```text
Input: s = "jar", t = "jam"
Output: false
Explanation: The characters do not match exactly.
```

---

## 📋 Constraints

- `1 <= s.length, t.length <= 5 * 10^4`
- `s` and `t` consist of lowercase English letters.

---

## 🎯 Objective

Design an algorithm that efficiently determines whether two strings are anagrams of each other.

**Recommended Time Complexity:** `O(n + m)`
**Recommended Space Complexity:** `O(1)`
