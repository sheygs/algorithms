from typing import List


# Time Complexity: O(n^2)
# Space Complexity: 0(1)
# def maxProfitBrute(prices: List[int]) -> int:

#     max_profit = 0

#     num_days = len(prices)

#     for buy_day_index in range(num_days):
#         for sell_day_index in range(buy_day_index + 1, num_days):

#             current_profit = prices[sell_day_index] - [buy_day_index]

#             if current_profit > max_profit:
#                 max_profit = current_profit

#     return max_profit


# Time Complexity:  O(n) — single pass through prices
# Space Complexity: O(1) — only two variables tracked
# Greedy Approach
def maxProfitGreedy(prices: List[int]) -> int:
    min_price = float("infinity")  # lowest buy price seen so far
    max_profit = 0  # best profit seen so far

    for price in prices:
        if price < min_price:
            min_price = price  # found a cheaper buy day, update it
        else:
            profit = price - min_price  # what if we sold today?
            max_profit = max(max_profit, profit)  # is this the best so far?

    return max_profit


# Two pointer
# Time Complexity: O(n) — single pass through prices
# Space Complexity: O(1) — only two variables tracked


def maxProfit(prices: List[int]):
    l, r = 0, 1  # left=buy, right=sell
    max_profit = 0

    while r < len(prices):
        if prices[l] < prices[r]:
            profit = prices[r] - prices[l]
            max_profit = max(max_profit, profit)
        else:
            l = r
        r += 1
    return max_profit


print(maxProfitGreedy([1, 5, 2, 6, 3, 7]))
print(maxProfit([10, 1, 5, 6, 7, 1]))
print(maxProfit([10, 8, 7, 5, 2]))
