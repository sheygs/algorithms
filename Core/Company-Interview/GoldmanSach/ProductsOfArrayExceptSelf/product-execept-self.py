from typing import List

"""
Time Complexity: O(n^2)
Space Complexity:
    - O(n) space for output array
    - O(1) for extra space
"""


def productExceptSelfBrute(nums: List[int]) -> List[int]:
    result = [0] * len(nums)

    for i in range(len(nums)):
        product = 1
        for j in range(len(nums)):
            if i == j:
                continue
            product *= nums[j]

        result[i] = product

    return result


"""
Time Complexity: O(n): — three passes: build prefix, build suffix, combine
Space Complexity: O(n) — explicit prefix and suffix arrays of size n
"""


def productExceptSelf(nums: List[int]) -> List[int]:
    n = len(nums)
    # prefix[i] stores the product of all elements to the LEFT of index i
    # nothing is to the left of index 0, so seed with 1 (multiplicative identity)
    pref = [1] * n  # or [0] * n

    # suffix[i] stores the product of all elements to the RIGHT of index i
    # nothing is to the right of the last index, so seed with 1
    suff = [1] * n  # or [0] * n

    # pref[0] = suff[n - 1] = 1

    # O(n)
    for i in range(1, n):
        # each prefix entry multiplies the previous prefix by the element before i
        pref[i] = nums[i - 1] * pref[i - 1]

    # O(n)
    for i in range(n - 2, -1, -1):
        # each suffix entry multiplies the next suffix by the element after i
        suff[i] = nums[i + 1] * suff[i + 1]

    # O(n)
    return [pref[i] * suff[i] for i in range(n)]


"""
    Time Complexity: O(n)
    Space Complexity:
        - O(1) extra space
        - O(1) for output array

    This O(1) space version skips the separate prefix and suffix arrays entirely —
    it builds the prefix directly into result, then multiplies the suffix in during
    the second pass using a running variable.
"""


def productExceptSelf_(nums: List[int]) -> List[int]:
    result = [1] * (len(nums))

    # prefix pass — result[i] holds product of everything to the LEFT
    prefix = 1
    for i in range(len(nums)):
        result[i] = prefix
        prefix *= nums[i]

    # suffix pass — multiply in product of everything to the RIGHT
    postfix = 1
    for i in range(len(nums) - 1, -1, -1):
        result[i] *= postfix
        postfix *= nums[i]

    return result


print(productExceptSelf_([1, 2, 3, 4]))
# print(productExceptSelfBrute([1, 2, 3, 4]))
# print(productExceptSelf_([1, 2, 3, 4]))
