from typing import List


# Brute Force
# Time Complexity:
# Space Complexity:
def three_sum(nums: List[int]) -> List[List[int]]:
    pass


# Optimal: Two Pointer
# Time Complexity: O(n^2)
# Space Complexity: O(1)/O(n) depending on the sorting algorithm
def three_sum_optimal(nums: List[int]) -> List[List[int]]:
    sum = []

    # sort for ordering
    nums.sort()

    for index, num in enumerate(nums):
        # we don't want to reuse the same value
        # in the same position twice
        if index > 0 and num == nums[index - 1]:
            continue

        # two-sum
        l, r = index + 1, len(nums) - 1

        while l < r:
            threeSum = num + nums[l] + nums[r]

            if threeSum > 0:
                r -= 1
            elif threeSum < 0:
                l += 1
            else:
                sum.append([num, nums[l], nums[r]])
                l += 1

                while nums[l] == nums[l - 1] and l < r:
                    l += 1
    return sum
