# Smallest Missing Positive Integer

## Problem Description

Write a function `solution(A)` that, given an array `A` of `N` integers, returns the smallest positive integer (greater than 0) that does **not** occur in `A`.

### Examples

- Given `A = [1, 3, 6, 4, 1, 2]`, the function should return `5`.
- Given `A = [1, 2, 3]`, the function should return `4`.
- Given `A = [−1, −3]`, the function should return `1`.

### Assumptions

- `N` is an integer within the range `[1..100,000]`
- Each element of array `A` is an integer within the range `[−1,000,000..1,000,000]`

---

## Solution Approach

The smallest missing positive integer must lie in the range `[1, N+1]` (where `N` is the length of the array).
This is because if all numbers from `1` to `N` are present, the answer is `N+1`; otherwise, the missing number is somewhere in `1..N`.

We can solve it efficiently in **O(N) time and O(N) space** by:

1. Collecting all positive integers from the array into a set.
2. Iterating `i` from `1` to `N+1` and returning the first `i` that is **not** in the set.

---

## Complexity

- **Time Complexity:** O(N) – one pass to build the set, plus at most `N+1` membership checks.
- **Space Complexity:** O(N) – for storing the positive numbers in a set.


