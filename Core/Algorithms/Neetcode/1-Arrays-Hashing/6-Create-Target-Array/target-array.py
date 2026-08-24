from typing import List


# Time Complexity: O(n^2)
# Space Complexity: O(n)
# Best for: n <= 1000
def createTargetArray(nums: List[int], positions: List[int]) -> List[int]:
    target = []

    if len(nums) != len(positions):
        # both not equal in length
        return []

    for num, index in zip(nums, positions):
        target.insert(index, num)

    return target


print(createTargetArray(nums=[0, 1, 2, 3, 4], positions=[0, 1, 2, 2, 1]))
# print(createTargetArray(nums=[1, 2, 3, 4, 0], positions=[0, 1, 2, 3, 0]))
# print(createTargetArray(nums=[1], positions=[0]))
