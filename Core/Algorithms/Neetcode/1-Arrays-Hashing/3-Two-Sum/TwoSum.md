# Two Sum

## 📌 Problem Description

Given an array of integers `nums` and an integer `target`, return the **indices** of the two numbers such that they add up to `target`.

You may assume that:

- Each input has **exactly one solution**.
- You may **not use the same element twice**.
- The answer can be returned in **any order**.

---

## 🧠 Topics

- Arrays
- Hash Tables

---

## 📝 Examples

### Example 1

```text
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
Explanation: nums[0] + nums[1] = 2 + 7 = 9
```

### Example 2

```text
Input: nums = [3,2,4], target = 6
Output: [1,2]
```

### Example 3

```text
Input: nums = [3,3], target = 6
Output: [0,1]
```

---

## 📋 Constraints

- `2 <= nums.length <= 10^4`
- `-10^9 <= nums[i] <= 10^9`
- `-10^9 <= target <= 10^9`
- Only one valid answer exists.

---

## 🎯 Objective

Design an algorithm that efficiently finds the two indices whose values sum up to the given `target`.
