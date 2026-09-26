# 238. Product of Array Except Self

## Topics

`Array` `Prefix Sum`

## Problem Statement

Given an integer array `nums`, return an array `answer` such that `answer[i]` is equal to the product of all elements of `nums` **except** `nums[i]`.

The product of any prefix or suffix of `nums` is **guaranteed to fit** in a 32-bit integer.

You must write an algorithm that runs in **O(n)** time and **without using the division operation**.

---

## Examples

**Example 1:**

```test
Input:  nums = [1, 2, 3, 4]
Output: [24, 12, 8, 6]
```

**Example 2:**

```test
Input:  nums = [-1, 1, 0, -3, 3]
Output: [0, 0, 9, 0, 0]
```

---

## Constraints

- `2 <= nums.length <= 10⁵`
- `-30 <= nums[i] <= 30`
- The product of any prefix or suffix of `nums` is **guaranteed to fit** in a 32-bit integer

---

## Intuition

The key insight is that for each position `i`, the answer is:

```test
answer[i] = (product of everything to the LEFT of i)
           × (product of everything to the RIGHT of i)
```

We can compute both of these in **two passes** using prefix and suffix products — no division needed.

---

## Approach: Prefix & Suffix Pass (Optimal)

### Step 1 — Left pass (prefix products)

Build an array where `left[i]` = product of all elements **before** index `i`.

```test
nums    = [1,  2,  3,  4]
left    = [1,  1,  2,  6]   ← left[0]=1 (nothing to left), left[1]=1, left[2]=1×2, left[3]=1×2×3
```

### Step 2 — Right pass (suffix products)

Traverse from right to left, multiplying a running `right` value into the result.

```test
right (running) starts at 1
i=3: answer[3] = left[3] × right = 6 × 1  = 6,  then right = right × nums[3] = 1×4 = 4
i=2: answer[2] = left[2] × right = 2 × 4  = 8,  then right = right × nums[2] = 4×3 = 12
i=1: answer[1] = left[1] × right = 1 × 12 = 12, then right = right × nums[1] = 12×2 = 24
i=0: answer[0] = left[0] × right = 1 × 24 = 24, then right = right × nums[0] = 24×1 = 24

Output: [24, 12, 8, 6] ✓
```

---

## Solution (Python)

```python
from typing import List

class Solution:
    def productExceptSelf(self, nums: List[int]) -> List[int]:
        n = len(nums)
        answer = [1] * n

        # Left pass: answer[i] holds product of all elements to the left of i
        prefix = 1
        for i in range(n):
            answer[i] = prefix
            prefix *= nums[i]

        # Right pass: multiply in product of all elements to the right of i
        suffix = 1
        for i in range(n - 1, -1, -1):
            answer[i] *= suffix
            suffix *= nums[i]

        return answer
```

---

## Walkthrough (Example 1)

```test
nums = [1, 2, 3, 4]

--- Left pass ---
i=0: answer=[1, 1, 1, 1], prefix=1  → answer[0]=1,  prefix becomes 1
i=1: answer=[1, 1, 1, 1], prefix=1  → answer[1]=1,  prefix becomes 2
i=2: answer=[1, 1, 2, 1], prefix=2  → answer[2]=2,  prefix becomes 6
i=3: answer=[1, 1, 2, 6], prefix=6  → answer[3]=6,  prefix becomes 24

After left pass: answer = [1, 1, 2, 6]

--- Right pass ---
i=3: answer[3] = 6 × 1  = 6,  suffix becomes 4
i=2: answer[2] = 2 × 4  = 8,  suffix becomes 12
i=1: answer[1] = 1 × 12 = 12, suffix becomes 24
i=0: answer[0] = 1 × 24 = 24, suffix becomes 24

Final output: [24, 12, 8, 6] ✓
```

---

## Complexity Analysis

|           | Complexity | Explanation                                      |
| --------- | ---------- | ------------------------------------------------ |
| **Time**  | O(n)       | Two linear passes over the array                 |
| **Space** | O(1)       | Output array doesn't count; no extra arrays used |

> Note: The output array `answer` is not counted as extra space per the problem convention.

---

## Why Not Use Division?

The naive approach would be:

1. Compute the total product of all elements
2. For each `i`, divide total by `nums[i]`

This **breaks** when any element is `0` (division by zero), and the problem explicitly forbids it. The prefix/suffix approach handles zeros naturally.

---

## Edge Cases

| Case         | Input               | Expected Output   | Handled? |
| ------------ | ------------------- | ----------------- | -------- |
| Single zero  | `[1, 0, 3, 4]`      | `[0, 12, 0, 0]`   | ✅ Yes   |
| Two zeros    | `[0, 0, 3, 4]`      | `[0, 0, 0, 0]`    | ✅ Yes   |
| Negatives    | `[-1, 1, 0, -3, 3]` | `[0, 0, 9, 0, 0]` | ✅ Yes   |
| All ones     | `[1, 1, 1, 1]`      | `[1, 1, 1, 1]`    | ✅ Yes   |
| Two elements | `[3, 4]`            | `[4, 3]`          | ✅ Yes   |

---

## GS Interview Tips

- **State the constraint upfront**: "The problem says no division, so I won't use total product divided by element."
- **Name the pattern clearly**: "I'll use a prefix-suffix product approach — two passes, O(n) time, O(1) space."
- **Narrate the passes separately**: Explain the left pass first, confirm the intermediate result, then explain the right pass. Don't try to explain both at once.
- **Test with a zero**: Voluntarily test `[1, 0, 3, 4]` to show you've considered the edge case.
- **Follow-up GS interviewers sometimes ask**: _"Can you do it in a single pass?"_ — the answer is no while maintaining O(1) space, since you need both left and right products.
