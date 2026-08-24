# Time Complexity:  O(n) — single pass through prices
# Space Complexity: O(1) — only two variables tracked
from typing import List


def maxProfit(prices: List[int], fee: int) -> int:
    # hold: best cash achievable while currently owning a stock
    # you spent prices[0], so net cash is negative
    hold = -prices[0]

    # free: best cash achievable while NOT owning a stock
    # haven't traded yet, no profit
    free = 0

    for i in range(1, len(prices)):
        # snapshot yesterday's values before updating
        prev_hold = hold
        prev_free = free

        # option 1: stay free (do nothing)
        # option 2: sell today — gain prices[i], pay fee, leave holding state
        free = max(prev_free, prev_hold + prices[i] - fee)

        # option 1: stay holding (do nothing)
        # option 2: buy today — spend prices[i], enter holding state
        # note: use prev_free so we don't buy and sell on the same day
        hold = max(prev_hold, prev_free - prices[i])

    # at the end, best profit is always in the free state
    # (holding stock at the end would mean we never sold — suboptimal)
    return free


# Tests
print(maxProfit([1, 3, 2, 8, 4, 9], fee=2))  # 8
print(maxProfit([1, 3, 7, 5, 10, 3], fee=3))  # 6
print(maxProfit([1, 2, 3, 4, 5], fee=1))  # 3
print(maxProfit([9, 8, 7, 6, 5], fee=1))  # 0  (falling prices, never trade)
print(maxProfit([1], fee=0))  # 0  (only one price, can't sell)
