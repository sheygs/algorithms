from typing import List


# TC: O(n)
# SC: O(n)
def dailyTemperatures(temperatures: List[int]) -> List[int]:
    n = len(temperatures)
    result = [0] * n
    stack = []  # pair: [(temp, index)]

    for index, temp in enumerate(temperatures):
        while stack and temp > stack[-1][0]:
            _, stackIndex = stack.pop()
            result[stackIndex] = index - stackIndex
        stack.append((temp, index))
    return result
