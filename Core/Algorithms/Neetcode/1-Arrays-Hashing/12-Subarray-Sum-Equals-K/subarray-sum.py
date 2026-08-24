from typing import List


# TC: O(n)
# SC: O(n)
def subarraySum(nums: List[int], k: int) -> int:
    result = 0
    curSum = 0
    # {prefixSum: count}
    # count: how many times the prefixsum occurs
    prefixSums = {0: 1}

    for num in nums:
        curSum += num
        diff = curSum - k

        result += prefixSums.get(diff, 0)
        prefixSums[curSum] = 1 + prefixSums.get(curSum, 0)

    return result


print(subarraySum([1, 1, 1], k=2))
print(subarraySum([1, 2, 3], k=3))
