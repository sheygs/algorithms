from typing import List

"""
Time Complexity: O(n+m log(n + m))
Space Complexity: O(n + m)
where n is the length of nums1 and m is the length of nums2
"""


def findMedianSortedArraysBrute(nums1: List[int], nums2: List[int]) -> float:
    # merge the two arrays together
    # sort them
    # if the length of the resulting array is even, get the two mid values, sum and divide by 2 and return it
    # if the //     //. //. //.      //.    //. // , get and return the mid value
    merged = nums1 + nums2
    merged.sort()
    total_length = len(merged)

    if total_length % 2 == 0:
        # even length: median with be the 2 middle values
        return (merged[total_length // 2] + merged[(total_length // 2) - 1]) / 2
    else:
        # odd length: median with be a single middle values
        return merged[total_length // 2]


"""
Time Complexity: O(log(min(n,m)))
Space Complexity: O(1)
where n is the length of nums1 and m is the length of nums2
"""


def findMedianSortedArrays(nums1: List[int], nums2: List[int]) -> float:
    A, B = nums1, nums2

    # total number of elements across both arrays
    total_length = len(nums1) + len(nums2)

    # how many elements belong in the combined left half
    half = total_length // 2

    # always binary search on the smaller array to minimise iterations
    # if A is larger, swap so A is always the shorter one
    if len(A) > len(B):
        A, B = B, A

    # binary search bounds on array A
    l, r = 0, len(A) - 1

    while True:
        # i = A's partition index (last element of A's left half)
        # using (l+r)//2 gives the midpoint of current search space
        i = (l + r) // 2

        # j = B's partition index, derived from i
        # -2 accounts for 0-based indexing of both i and j
        # (i+1) elements from A + (j+1) elements from B = half
        # so j = half - (i+1) - 1 = half - i - 2
        j = half - i - 2

        # left and right boundary values for A's partition
        # if i < 0, A contributes nothing to the left half → -inf sentinel
        # if i+1 >= len(A), A contributes nothing to the right half → +inf sentinel
        Aleft = A[i] if i >= 0 else float("-infinity")
        Aright = A[i + 1] if (i + 1) < len(A) else float("infinity")

        # left and right boundary values for B's partition
        # same sentinel logic applies for B's out-of-bounds indices
        Bleft = B[j] if j >= 0 else float("-infinity")
        Bright = B[j + 1] if (j + 1) < len(B) else float("infinity")

        # correct partition found when:
        #   max of A's left  ≤ min of B's right  (Aleft ≤ Bright)
        #   max of B's left  ≤ min of A's right  (Bleft ≤ Aright)
        # this guarantees every left element ≤ every right element
        if Aleft <= Bright and Bleft <= Aright:

            # odd total length → median is the smallest value in the right half
            if total_length % 2 != 0:
                return min(Aright, Bright)

            # even total length → median is average of the two middle values
            # largest of the left boundaries + smallest of the right boundaries
            else:
                return (max(Aleft, Bleft) + min(Aright, Bright)) / 2

        # Aleft is too large → A's partition is too far right → move left
        elif Aleft > Bright:
            r = i - 1

        # Bleft is too large → A's partition is too far left → move right
        else:
            l = i + 1


# print(findMedianSortedArraysBrute([1, 2], [2]))
print(findMedianSortedArrays([1, 2], [2]))
print(findMedianSortedArrays([1, 3], [2, 4]))
