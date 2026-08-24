from typing import List


# Time Complexity: O(nlogn)
# Space Complexity: O(1) or O(n) depending on the sorting algorithm
def eraseOverlapIntervals(intervals: List[List[int]]) -> int:
    # Sort intervals by start time. This allows us to process them
    # linearly and only compare the current interval with the last "kept" one.
    intervals.sort()

    count = 0
    # prevEnd tracks the end time of the last interval we decided to keep.
    prevEnd = intervals[0][1]

    for start, end in intervals[1:]:
        # CASE 1: No Overlap
        # The current interval starts after (or when) the previous one ends.
        if start >= prevEnd:
            # We "keep" this interval, so update the boundary to its end time.
            prevEnd = end

        # CASE 2: Overlap Detected
        else:

            # We must remove one of the two overlapping intervals.
            count += 1

            # example: [[1,10], [2,3]]
            # GREEDY CHOICE: To minimize future overlaps, we want to keep the
            # interval that ends EARLIER.
            # If the current 'end' is smaller than 'prevEnd', it means the current
            # interval is "shorter" or finishes sooner, so we "discard" the old
            # one and track this new, smaller end time instead.
            prevEnd = min(end, prevEnd)
    return count
