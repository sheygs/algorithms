from typing import List


# TC: O(n)
# SC: O(n)
def missingNumber(nums: List[int]) -> int:
    num_set = set(nums)
    n = len(nums)

    for i in range(n + 1):
        if i not in num_set:
            return i


# TC: O(n)
# SC: O(1)
# def missingNumber(nums: List[int]) -> int:
#     res = len(nums)

#     for i in range(len(nums)):
#         res += i - nums[i]
#     return res


print(missingNumber([3, 0, 1]))
print(missingNumber([0, 1]))
print(missingNumber([9, 6, 4, 2, 3, 5, 7, 0, 1]))
