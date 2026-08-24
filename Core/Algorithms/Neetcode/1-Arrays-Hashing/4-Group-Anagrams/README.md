# Group Anagrams

**Difficulty:** Medium
**Company:** Visa
**Topics:** Arrays, Strings

---

## Problem Statement

Given an array of strings `strs`, group all anagrams together and return the grouped result. The answer can be returned in any order.

> Two strings are **anagrams** if one can be rearranged to form the other (same characters, same frequency).

---

## Examples

### Example 1

```text
Input:  strs = ["eat","tea","tan","ate","nat","bat"]
Output: [["bat"],["nat","tan"],["ate","eat","tea"]]
```

> - `"bat"` has no anagram counterpart in the array.
> - `"nat"` and `"tan"` are anagrams of each other.
> - `"ate"`, `"eat"`, and `"tea"` are all anagrams of each other.

### Example 2

```text
Input:  strs = [""]
Output: [[""]]
```

### Example 3

```text
Input:  strs = ["a"]
Output: [["a"]]
```

---

## Constraints

| Property      | Bound                          |
| ------------- | ------------------------------ |
| Array length  | `1 <= strs.length <= 10⁴`      |
| String length | `0 <= strs[i].length <= 100`   |
| Characters    | Lowercase English letters only |
