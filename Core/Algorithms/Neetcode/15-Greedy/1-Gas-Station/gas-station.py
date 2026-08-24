from typing import List


# TC: O(n)
# SC: O(1)
def canCompleteCircuit(gas: List[int], cost: List[int]) -> int:
    tank_total = 0
    index = 0

    if sum(gas) < sum(cost):
        return -1

    for i in range(len(gas)):
        tank_total += gas[i] - cost[i]

        if tank_total < 0:
            tank_total, index = 0, i + 1

    return index
