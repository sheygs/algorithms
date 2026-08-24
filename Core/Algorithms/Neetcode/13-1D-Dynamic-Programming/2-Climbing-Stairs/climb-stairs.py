# TC: O(n)
# SC: O(1)


# Dynamic Programming: (Space Optimised) bottom-up approach
def climbStairs(n: int) -> int:
    # intuition: Ways(n) = Ways(n-1) + Ways(n-2)
    # base case: if there are no stairs or just 1, there's only 1 way.
    if n <= 1:
        return 1

    # ways_to_reach_n_minus_1 (initially step 1)
    # ways_to_reach_n_minus_2 (initially step 0)
    current_step_ways = 1
    prev_step_ways = 1

    # We iterate from the 2nd step up to n
    for _ in range(2, n + 1):
        # The ways to reach the current step is the sum of the previous two
        total_ways = current_step_ways + prev_step_ways

        # Shift the window forward:
        # The old 'current' becomes the 'previous' for the next step
        prev_step_ways = current_step_ways
        # The 'total' we just found becomes the 'current'
        current_step_ways = total_ways

    return current_step_ways
