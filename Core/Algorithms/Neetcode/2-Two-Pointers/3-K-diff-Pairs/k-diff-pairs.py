from typing import List


"""
The naive idea is to check every pair (i, j) where i != j and see if
their absolute difference equals k. Use a set to store unique pairs and avoid duplicates.
"""


# Time Complexity: O(n²) — nested loops over all pairs
# Space Comeplexity: O(n) — for the set storing unique pairs
def findPairsBrute(nums: List[int], k: int) -> int:
    unique_pairs = set()

    for i in range(len(nums)):
        for j in range(len(nums)):
            if i != j and abs(nums[i] - nums[j]) == k:
                unique_pairs.add((min(nums[i], nums[j]), max(nums[i], nums[j])))

    return len(unique_pairs)


# Optimal
# Time Complexity: O(n) one pass to build the map, one pass over unique keys
# Space Complexity: O(n) for the frequency map
def findPairs(nums: List[int], k: int) -> int:
    freq = {}
    count = 0

    # step 1: build frequency map
    for num in nums:
        freq[num] = freq.get(num, 0) + 1

    for n in freq:
        if k == 0:
            # need the same number at least twice
            if freq[n] > 1:
                count += 1
        else:
            # need num + k to exist anywhere in the array
            if n + k in freq:
                count += 1

    return count


# Optimal (Two Pointer)
# Time Complexity: O(n log n) due to sorting
# Space Complexity: O(1)
def findPairs_(nums: List[int], k: int) -> int:
    nums.sort()
    left, right = 0, 1
    count = 0

    while right < len(nums):
        diff = nums[right] - nums[left]
        if left == right or diff < k:
            right += 1
        elif diff > k:
            left += 1
        else:
            count += 1
            left += 1
            while left < len(nums) and nums[left] == nums[left - 1]:
                left += 1

    return count


#print(findPairs([3, 1, 4, 1, 5], k=2))
# print(findPairs([1, 2, 3, 4, 5], k=1))
print(findPairs([1, 3, 1, 5, 4], k=0))
