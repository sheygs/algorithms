from typing import List


# Time Complexity:  O(n) — one pass to build set, one pass to scan
# Space Complexity: O(n) — set stores all n numbers
from typing import List


def firstMissingPositive_set(nums: List[int]) -> int:
    # dump all values into a set for O(1) lookups
    seen = set(nums)

    # scan 1, 2, 3, ... n in order
    # first number not in the set is the answer
    for i in range(1, len(nums) + 1):
        if i not in seen:
            return i

    # all of 1..n are present → answer is n+1
    return len(nums) + 1


# Tests
print(firstMissingPositive_set([1, 2, 0]))  # 3
print(firstMissingPositive_set([3, 4, -1, 1]))  # 2
print(firstMissingPositive_set([7, 8, 9, 11, 12]))  # 1
print(firstMissingPositive_set([1, 2, 3]))  # 4
print(firstMissingPositive_set([1]))  # 2

# Time Complexity:  O(n) — three passes, each O(n)
# Space Complexity: O(1) — modified in-place, no extra structures


def firstMissingPositive(nums: List[int]) -> int:
    n = len(nums)

    # ── Step 1: sanitise ─────────────────────────────────────────────────────
    # replace any number that can't be an answer (<=0 or >n) with n+1
    # so they don't interfere with our marking step
    for i in range(n):
        if nums[i] <= 0 or nums[i] > n:
            nums[i] = n + 1

    # ── Step 2: mark presence ────────────────────────────────────────────────
    # for each number in [1, n], mark its corresponding index negative
    # use abs() when reading because a value may already be marked negative
    for i in range(n):
        val = abs(nums[i])
        if 1 <= val <= n:
            idx = val - 1  # number 1 → index 0, number 3 → index 2
            if nums[idx] > 0:  # only negate if not already negative
                nums[idx] = -nums[idx]

    # ── Step 3: find first unmarked index ────────────────────────────────────
    # the first index that is still positive means that number is missing
    for i in range(n):
        if nums[i] > 0:
            return i + 1  # index 0 → number 1, index 2 → number 3

    # all numbers 1..n are present → answer is n+1
    return n + 1


# Tests
print(firstMissingPositive([1, 2, 0]))  # 3
print(firstMissingPositive([3, 4, -1, 1]))  # 2
print(firstMissingPositive([7, 8, 9, 11, 12]))  # 1
print(firstMissingPositive([1, 2, 3]))  # 4
print(firstMissingPositive([1]))  # 2
