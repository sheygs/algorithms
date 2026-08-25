# Optimal
# Time Complexity:  O(log n) — binary search halves the search space each iteration
# Space Complexity: O(1)     — no extra data structures used
from typing import List


def search(nums: List[int], target: int) -> int:

    l, r = 0, len(nums) - 1

    while l <= r:
        mid = (l + r) // 2

        # target found, return its index immediately
        if target == nums[mid]:
            return mid

        # ── Left half is sorted (nums[l] <= nums[mid]) ──────────────────────
        if nums[l] <= nums[mid]:
            # target is out of the sorted left half's range → search right half
            # condition: target is above mid  OR  target is below the left boundary
            if target > nums[mid] or target < nums[l]:
                l = mid + 1
            else:
                # target lies within the sorted left half → search left half
                r = mid - 1

        # ── Right half is sorted (nums[mid] < nums[l]) ──────────────────────
        else:
            # target is out of the sorted right half's range → search left half
            # condition: target is below mid  OR  target is above the right boundary
            if target < nums[mid] or target > nums[r]:
                r = mid - 1
            else:
                # target lies within the sorted right half → search right half
                l = mid + 1

    # target not found in the array
    return -1


print(search([4, 5, 6, 7, 0, 1, 2], target=0))
