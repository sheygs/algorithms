from typing import List


# Time Complexity: O(n^2)
# Space Complexity: O(1)


def minSubArrayLenBrute(target: int, nums: List[int]) -> int:
    n = len(nums)
    size = float("inf")

    for i in range(n):
        curSum = 0
        for j in range(i, n):
            curSum += nums[j]
            if curSum >= target:
                size = min(size, j - i + 1)
                break

    return 0 if size == float("inf") else size


# Time Complexity:  O(n) — l and r each traverse the array at most once
# Space Complexity: O(1) — only pointers and running sum


def minSubArrayLen(target: int, nums: List[int]) -> int:
    l = 0
    total = 0
    min_len = float("infinity")  # start at infinity so any valid window beats it

    for r in range(len(nums)):
        total += nums[r]  # expand window by including nums[r]

        # window is valid — try shrinking it to find smaller answer
        while total >= target:
            min_len = min(min_len, r - l + 1)  # record current window size
            total -= nums[l]  # remove leftmost element
            l += 1  # shrink from the left

    # if min_len never updated, no valid subarray exists
    return 0 if min_len == float("infinity") else min_len


print(minSubArrayLen(10, [2, 1, 5, 1, 5, 3]))
print(minSubArrayLen(5, [1, 2, 1]))
