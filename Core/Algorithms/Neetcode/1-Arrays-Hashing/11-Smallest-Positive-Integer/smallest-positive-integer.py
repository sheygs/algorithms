# TC: O(N), SC: O(N)
from typing import List


def smallest_positive_integer(A):
    # Create a set of all positive numbers present in the array
    positive_nums = {x for x in A if x > 0}

    # Check from 1 upwards until we find the smallest missing positive integer
    i = 1
    while i in positive_nums:
        i += 1
    return i


print(smallest_positive_integer([1, 3, 6, 4, 1, 2]))
print(smallest_positive_integer([1, 2, 3]))
print(smallest_positive_integer([-1, -3]))


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
