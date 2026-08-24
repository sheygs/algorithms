from typing import List


# Brute Force
# Time Complexity:
# Space Complexity:
def twoSum():
    pass


# Optimal: Two Pointer
# Time Complexity: O(n)
# Space Complexity: O(1)
def twoSumOptimal(numbers: List[int], target: int) -> List[int]:
    l, r = 0, len(numbers) - 1

    while l < r:
        curSum = numbers[l] + numbers[r]

        if curSum > target:
            r -= 1
        elif curSum < target:
            l += 1
        else:
            return [l + 1, r + 1]
    return []
