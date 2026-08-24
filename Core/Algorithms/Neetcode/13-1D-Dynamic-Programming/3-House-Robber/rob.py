from typing import List


# TC: O(n)
# SC: O(1)


# Dynamic Programming: (Space Optimised)
def rob(nums: List[int]) -> int:
    """
    Determines the maximum amount of money that can be robbed without
    alerting the police (cannot rob adjacent houses).
    """
    # rob_prev_prev: Max money if we stopped two houses back
    # rob_prev: Max money if we stopped at the previous house
    rob_prev_prev, rob_prev = 0, 0

    for current_house_value in nums:
        # We have two choices:
        # 1. Rob current house + max from two houses ago
        # 2. Skip current house and keep the max from the previous house
        current_max = max(current_house_value + rob_prev_prev, rob_prev)

        # Shift the window forward
        rob_prev_prev, rob_prev = rob_prev, current_max

    return rob_prev


print(rob([1, 1, 3, 3]))
print(rob([2, 9, 8, 3, 6]))
