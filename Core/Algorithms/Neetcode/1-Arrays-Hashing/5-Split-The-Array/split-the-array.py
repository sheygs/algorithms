from typing import List


# Brute Force
# Time Complexity: O(n^2)
# Space Complexity: O(1)
def isPossibleToSplit(numbers: List[int]) -> bool:
    pass


# Optimal
# Time Complexity: O(n)
# Space Complexity: O(n)
def isPossibleToSplitOptimal(numbers: List[int]) -> bool:
    # intuition: we don't have to care about the length of the 2 arrays
    counter = {}

    if len(numbers) % 2 != 0:
        raise "`numbers` must be even in length"

    # Build frequency map
    for n in numbers:
        counter[n] = 1 + counter.get(n, 0)

    # Any element appearing 3+ times makes it impossible
    for num in counter:
        if counter[num] > 2:
            return False
    return True


print(isPossibleToSplitOptimal([1, 1, 2, 2, 3, 4, 9]))
