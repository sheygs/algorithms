from typing import List


# Brute
# Time Complexity: O(n)
# Space Complexity: O(1)
def search_brute(nums: List[int], target: int) -> int:
    for i in range(len(nums)):
        if nums[i] == target:
            return i
    return -1


# Optimal
# Time Complexity:  O(log n) — binary search halves the search space each iteration
# Space Complexity: O(1)     — no extra data structures used
def findMin(nums: List[int]) -> int:

    l, r = 0, len(nums) - 1

    minimum = nums[0]

    while l <= r:
        if nums[l] < nums[r]:
            minimum = min(nums[l], minimum)
            break

        m = (l + r) // 2
        minimum = min(nums[m], minimum)

        if nums[m] >= nums[l]:
            l = m + 1
        else:
            r = m - 1

    return minimum


print(findMin([3, 5, 6, 0, 1, 2]))
print(findMin([3, 4, 5, 6, 1, 2]))
