from typing import List


# TC: O(n), SC: O(n)
def productExceptSelf(nums: List[int]) -> List:
    n = len(nums)

    prefix = [1] * n
    suffix = [1] * n

    for i in range(1, n):
        prefix[i] = nums[i - 1] * prefix[i - 1]

    for i in range(n - 2, -1, -1):
        suffix[i] = nums[i + 1] * suffix[i + 1]

    return [prefix[i] * suffix[i] for i in range(n)]


print(productExceptSelf([1, 2, 3, 4]))
