from typing import List


# Brute force
# Time Complexity: O(n^2)
# Space Complexity: 0(1)
def maxProfitBrute(prices: List[int]) -> int:

    max_profit = 0

    num_days = len(prices)

    for buy_day_index in range(num_days):
        for sell_day_index in range(buy_day_index + 1, num_days):

            current_profit = prices[sell_day_index] - [buy_day_index]

            if current_profit > max_profit:
                max_profit = current_profit

    return max_profit


# Optimal - Two pointer
# Time Complexity: O(1)
# Space Complexity: O(n)


def maxProfit(prices: List[int]):
    l, r = 0, 1
    max_profit = 0

    while r < len(prices):
        if prices[l] < prices[r]:
            profit = prices[r] - prices[l]
            max_profit = max(max_profit, profit)
        else:
            l = r
        r += 1
    return max_profit


# Dynamic Programming
# def max_profit_optimal_v2(prices: List[int]) -> int:
#     maxP = 0
#     minBuy = prices[0]

#     for sell in prices:
#         maxP = max(maxP, sell - minBuy)
#         minBuy = min(minBuy, sell)
#     return maxP


print(maxProfit([10, 1, 5, 6, 7, 1]))
# print(maxProfitOptimal([10, 8, 7, 5, 2]))
