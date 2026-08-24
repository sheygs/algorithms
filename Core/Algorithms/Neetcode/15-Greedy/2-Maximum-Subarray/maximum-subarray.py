import math
from typing import List

# Time Complexity:  O(n) — single pass through the array
# Space Complexity: O(1) — only two variables tracked


# kadane's algorithm
def maxSubArray(nums: List[int]) -> int:
    # seed with nums[0] — handles the case where all numbers are negative
    # if we used 0, we'd incorrectly return 0 for [-3,-1,-2]
    # maxSub = nums[0]
    maxSub = -math.inf
    curSum = 0  # running sum of the current subarray

    for n in nums:
        # a negative running sum is a liability — it drags down whatever comes next
        # reset to 0 and start a fresh subarray from the current element
        if curSum < 0:
            curSum = 0

        curSum += n  # extend the current subarray with n
        maxSub = max(maxSub, curSum)  # is this the best subarray seen so far?

    return maxSub


# Tests
print(maxSubArray([-2, 1, -3, 4, -1, 2, 1, -5, 4]))  # 6   → [4,-1,2,1]
print(maxSubArray([1]))  # 1
print(maxSubArray([5, 4, -1, 7, 8]))  # 23  → [5,4,-1,7,8]
print(maxSubArray([-3, -1, -2]))  # -1  → [-1] (least negative)
print(maxSubArray([-2, 1]))  # 1
