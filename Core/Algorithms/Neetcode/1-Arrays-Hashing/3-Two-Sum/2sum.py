from typing import List


# Brute force
# Time Complexity: O(n^2)
# Space Complexity: O(1)
def twoSum(nums: List[int], target: int):
    if not isinstance(target, int):
        return
    if not isinstance(nums, list):
        return
    for i in range(len(nums)):
        for j in range(i + 1, len(nums)):
            if nums[i] + nums[j] == target:
                return [i, j]
    return []


# Optimal
# Time Complexity: O(n)
# Space Complexity: O(n)
def twoSumOptimal(nums: List[int], target: int):
    prevMap = {}  # val:index

    for index, curr_num in enumerate(nums):
        diff = target - curr_num

        if diff in prevMap:
            return [prevMap[diff], index]

        prevMap[curr_num] = index

    return []


print(twoSumOptimal([2, 7, 11, 15], 9))
# print(twoSumOptimal([3, 2, 4], target=6))
# print(twoSumOptimal([3, 3], 6))
