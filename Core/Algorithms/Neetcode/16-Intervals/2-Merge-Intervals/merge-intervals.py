from typing import List


def mergeBrute(intervals: List[List[int]]) -> List[List[int]]:
    pass


"""
Optimal
Time complexity: O(nlogn)
Space complexity:
- O(1) or O(n) space depending on the sorting algorithm
- O(n) for the output list.
"""


def merge(intervals: List[List[int]]) -> List[List[int]]:

    # sort by first value O(nlogn)
    intervals.sort(key=lambda i: i[0])

    output = [intervals[0]]

    for start, end in intervals[1:]:
        # get the end value of the most recntly added interval
        lastEnd = output[-1][1]

        if start <= lastEnd:
            output[-1][1] = max(lastEnd, end)
        else:
            output.append([start, end])

    return output
