from typing import List

# def trap_brute_force(height: List[int]) -> int:
#     """
#     Time Complexity: O(n^2) (quadratic) because of the nested scanning behavior
#     Space Complexity: O(1) (constant because the amount of extra memory used does not grow with the size of the input array)

#     Intuition:
#     For each position, the water trapped above it depends on the tallest bar to its left and the tallest bar to its right.
#     If we know these two values, the water at index i is: min(leftMax, rightMax) - height[i]

#     The brute-force method recomputes the left maximum and right maximum for every index by scanning the array each time.
#     """

#     if not isinstance(height, list):
#         return 0

#     if height == 0:
#         return 0

#     n = len(height)
#     max_amount = 0

#     for i in range(n):
#         leftmax = rightmax = height[i]

#         for j in range(i):
#             leftmax = max(leftmax, height[j])

#         for j in range(i + 1, n):
#             rightmax = max(rightmax, height[j])

#         max_amount += min(leftmax, rightmax) - height[i]

#     return max_amount


"""
"Always process the shorter side, because the taller side is already guaranteed to hold water in."
Water at any position is determined by the shorter of the two surrounding walls. If you're standing at some index and the left max is 3 and the right max is 7, the water level is capped at 3 — the right side doesn't matter anymore, it's tall enough. So you focus on the left. You only ever need to think about one side at a time — the weaker one.

"Move the pointer first, update the max."
The endpoints can never trap water — there's nothing outside them to form a wall. So they're pre-loaded into leftMax and rightMax before the loop. That means the loop should start at index 1 and n-2, not 0 and n-1. Moving the pointer before reading is just a compact way of saying "skip the endpoint, start from the next one in."

"Add the difference — which is automatically zero if you just hit a new max, so you never need a separate check."
If the new bar is taller than leftMax, then leftMax gets updated to that bar's height. So when you do leftMax - height[l], you're doing height[l] - height[l] — which is zero. Nothing added. If the bar is shorter, leftMax stays the same and the difference is the trapped water. Either way the same line handles it — no if needed.
"""


def trap(height: List[int]) -> int:
    """
    Time Complexity: O(n)
    Space Complexity: O(1)
    """

    # first, just bail early if there's nothing in the list
    if not height or len(height) == 0:
        return 0

    # put one pointer at the far left, one at the far right
    left, right = 0, len(height) - 1

    # and remember the tallest wall each pointer has seen so far
    # we seed these with the actual end values, not zero,
    # because the loop moves the pointer BEFORE reading —
    # so we need the endpoints to already be "seen"
    leftMax, rightMax = height[left], height[right]

    water = 0  # this is where we accumulate the total trapped water

    while left < right:  # keep going until the two pointers meet

        if leftMax < rightMax:
            # the left wall is shorter, so the left side is the bottleneck
            # we don't need to worry about the right — it's tall enough
            # so move the left pointer inward to the next position
            left += 1

            # now update leftMax — did we just find a taller wall?
            leftMax = max(leftMax, height[left])

            # here's the trick: if height[l] was a new max,
            # leftMax - height[l] = 0, so we add nothing
            # if height[l] was lower, we get the water above it
            # same line handles both cases
            water += leftMax - height[left]

        else:
            # the right wall is shorter (or they're equal — either side works)
            # so now the right side is the bottleneck, mirror everything
            right -= 1

            rightMax = max(rightMax, height[right])

            # same trick — zero if new max, positive if water is trapped
            water += rightMax - height[right]

    # l and r have met, every index has been visited exactly once
    return water


print(f"result: {trap([0,2,0,3,1,0,1,3,2,1])}")
# print(f"result: {trap_brute_force([0,2,0,3,1,0,1,3,2,1])}")
