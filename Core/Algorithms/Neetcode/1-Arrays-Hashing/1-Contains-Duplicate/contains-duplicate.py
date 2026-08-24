from typing import List

"""
 Time Complexity: O(n^2)
 Space Complexity: O(1)
"""


def hasDuplicateBrute():
    pass


"""
 Time Complexity: O(n)
 Space Complexity: O(n)
"""


# Optimal
def hasDuplicate(nums: List[int]) -> bool:
    freq = set()

    for num in nums:
        if num in freq:
            return True
        freq.add(num)
    return False
