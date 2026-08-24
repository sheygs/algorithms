from typing import List


# Time Complexity: O(2^n)
# Space Complexity: O(n)
def maxProfitBrute(prices: List[int]) -> int:
    pass


# Greedy Approach
# Time Complexity: O(n)
# Space Complexity: O(1)
def maxProfit(prices: List[int]) -> int:
    profit = 0

    for i in range(1, len(prices)):
        if prices[i] > prices[i - 1]:
            profit += prices[i] - prices[i - 1]

    return profit


print(maxProfit([1, 2, 3, 4, 5]))
print(maxProfit([7, 6, 4, 3, 1]))
