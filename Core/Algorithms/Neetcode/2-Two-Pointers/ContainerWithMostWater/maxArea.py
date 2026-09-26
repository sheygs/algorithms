from typing import List


# Time: O(n^2) Space: O(1)
def maxAreaBrute(heights: List[int]) -> int:
    max_area = 0

    for l in range(len(heights)):
        for r in range(l + 1, len(heights)):
            # area = l * b
            area = min(heights[l], heights[r]) * (r - l)
            max_area = max(max_area, area)
    return max_area


# print(maxAreaBrute([1, 7, 2, 5, 4, 7, 3, 6]))
# print(maxAreaBrute([2, 2, 2]))


# Time: O(n) Space: O(1)
def maxArea(heights: List[int]) -> int:
    max_area = 0
    l, r = 0, len(heights) - 1

    while l < r:
        area = (r - l) * min(heights[l], heights[r])
        max_area = max(max_area, area)

        # if heights[l] < heights[r]:
        #     # we want to maximise both of the heights
        #     l += 1
        # elif heights[l] > heights[r]:
        #     r -= 1
        # else:
        #  # equal
        #    l += 1 # or r -= 1

        if heights[l] < heights[r]:
            # we want to maximise both of the heights
            l += 1
        else:  # covers both greater AND equal
            r -= 1

    return max_area


print(maxArea([1, 7, 2, 5, 4, 7, 3, 6]))
#print(maxArea([2, 2, 2]))
