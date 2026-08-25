from typing import List

"""
Time Complexity: O(n)
Space Complexity: O(1)
"""


def findMinBrute(nums: List[int]) -> int:
    if not nums:
        return None

    minimum = nums[0]
    for i in range(1, len(nums)):
        if nums[i] < minimum:
            minimum = nums[i]
    return minimum


def findMin_(nums):
    return min(nums) if nums else None


"""
Time Complexity: O(logn)
Space Complexity: O(1)
"""


def findMin(nums: List[int]) -> int:
    minimum = nums[0]  # arbitrary value
    l, r = 0, len(nums) - 1

    while l <= r:
        # when array is already linearly sorted i.e. [1,2,3,4,5,6]
        # just return the minimum value
        if nums[l] < nums[r]:
            minimum = min(minimum, nums[l])
            break

        # here, the array is not linearly sorted but rotated
        m = (l + r) // 2
        minimum = min(minimum, nums[m])

        # we want to know if the mid value is a part of the LSP or RSP
        if nums[m] >= nums[l]:
            # search at the right sorted portion
            l = m + 1
        else:
            # search at the left sorted portion
            r = m - 1
    return minimum


def findMinTest(input: tuple):
    value, nums = input

    if not value:
        return nums

    minimum = nums[0]  # or 0

    l, r = 0, len(nums) - 1

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


# print(f"1: {findMin([4, 5, 6, 7, 0, 1, 2])}")
# print(f"2: {findMin([0, 1, 2, 4, 5, 6, 7])}")
# print(findMinBrute([3, 4, 5, 1, 2]))

testInputs = [(1, [3, 4, 5, 1, 2]), (None, []), (1, [2, 4, 3])]

for input in testInputs:
    print(findMinTest(input))
