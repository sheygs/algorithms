# Find Minimum in Rotated Sorted Array

## Topics: Array, Binary Search

## Problem

Suppose an array of length `n` sorted in ascending order is **rotated between 1 and n times**.

For example:

```text
Original: [0,1,2,4,5,6,7]

Rotate 4 times → [4,5,6,7,0,1,2]
Rotate 7 times → [0,1,2,4,5,6,7]
```

Rotating an array `[a0, a1, a2, ..., a(n-1)]` one time results in:

```text
[a(n-1), a0, a1, a2, ..., a(n-2)]
```

Given a rotated sorted array `nums` with **unique elements**, return the **minimum element**.

Your algorithm must run in **O(log n)** time.

---

## Examples

### Example 1

```text
Input: nums = [3,4,5,1,2]
Output: 1
```

Explanation:

```text
Original array: [1,2,3,4,5]
Rotated 3 times
```

---

### Example 2

```text
Input: nums = [4,5,6,7,0,1,2]
Output: 0
```

Explanation:

```text
Original array: [0,1,2,4,5,6,7]
Rotated 4 times
```

---

### Example 3

```text
Input: nums = [11,13,15,17]
Output: 11
```

Explanation:

```text
Array was rotated 4 times (same as original)
```

---

## Constraints

```text
1 <= nums.length <= 5000
-5000 <= nums[i] <= 5000
All elements in nums are unique
nums is sorted and rotated between 1 and n times
```

---

## Approach

A rotated sorted array consists of **two sorted halves**.

Example:

```text
[4,5,6,7,0,1,2]
```

The smallest value occurs where the **rotation happens**.

We can use **Binary Search** to locate this point efficiently.

Key observations:

- If `nums[mid] > nums[right]`, the minimum lies **to the right of mid**.
- If `nums[mid] <= nums[right]`, the minimum lies **at mid or to the left**.

This allows us to discard half of the search space each iteration.

---

## Complexity Analysis

### Time Complexity

```text
O(log n)
```

Binary search halves the search space at every step.

### Space Complexity

```text
O(1)
```

No extra memory is used.

---

## Edge Cases

### Array not rotated

```text
[1,2,3,4,5]
```

Minimum is the first element.

### Single element

```text
[10]
```

Return `10`.

### Rotation near the end

```text
[2,3,4,5,1]
```

Binary search still correctly finds `1`.

---

## Key Takeaway

The problem can be solved efficiently by leveraging the **sorted structure of the rotated array** and applying **binary search** to locate the rotation point.
