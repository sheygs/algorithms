# Create Target Array in the Given Order

**Difficulty:** Easy

---

## Topics

Array, Simulation

---

## Problem Statement

You are given two integer arrays `nums` and `index` of the same length. Your task is to create a **target array** under the following rules:

- Initially, the target array is **empty**.
- From left to right, read `nums[i]` and `index[i]`, insert the value `nums[i]` at position `index[i]` in the target array.
- Repeat until there are no elements left to read in `nums` and `index`.

Return the **target array**. It is guaranteed that all insertion operations will be valid.

---

## Examples

**Example 1:**

```text
Input:  nums = [0,1,2,3,4], index = [0,1,2,2,1]
Output: [0,4,1,3,2]
```

> **Explanation:**
>
> ```text
> nums    index    target
>  0        0      [0]
>  1        1      [0,1]
>  2        2      [0,1,2]
>  3        2      [0,1,3,2]
>  4        1      [0,4,1,3,2]
> ```

---

**Example 2:**

```text
Input:  nums = [1,2,3,4,0], index = [0,1,2,3,0]
Output: [0,1,2,3,4]
```

> **Explanation:**
>
> ```text
> nums    index    target
>  1        0      [1]
>  2        1      [1,2]
>  3        2      [1,2,3]
>  4        3      [1,2,3,4]
>  0        0      [0,1,2,3,4]
> ```

---

**Example 3:**

```text
Input:  nums = [1], index = [0]
Output: [1]
```

---

## Constraints

- `1 <= nums.length, index.length <= 100`
- `nums.length == index.length`
- `0 <= nums[i] <= 100`
- `0 <= index[i] <= i`
