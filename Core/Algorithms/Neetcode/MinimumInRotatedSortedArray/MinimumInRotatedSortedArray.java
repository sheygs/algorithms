package GoldmanSach.MinimumInRotatedSortedArray;

public class MinimumInRotatedSortedArray {
    /**
     * Finds the minimum element in a rotated sorted array.
     * Time Complexity: O(log N) - Classic Binary Search efficiency.
     * Space Complexity: O(1) - Iterative approach with constant variables.
     */
    public int findMin(int[] nums) {
        // 1. Validation: Prevent processing invalid inputs
        if (nums == null || nums.length == 0) {
            // throw new IllegalArgumentException("Input array must not be null or empty");
            return -1;
        }

        int minimum = nums[0];
        int left = 0, right = nums.length - 1;

        /*
        * We use 'left <= right' because the loop updates an external 'minimum' variable.
        * This ensures that when the search space narrows to a single element (left == right),
        * that element is processed as 'mid' and checked against our minimum before
        * the pointers cross and the loop terminates.
        */
        while (left <= right) {
            /*
            * 2. Shortcut Check:
            * If nums[left] < nums[right], the current window [left...right]
            * is already fully sorted. The minimum in this window MUST be nums[left].
            */
            if (nums[left] < nums[right]) {
                minimum = Math.min(minimum, nums[left]);
                break;
            }

            // 3. Middle Calculation:
            // Using mid = left + (right - left) / 2 is safer for extremely large arrays.
            int mid = (left + right) / 2;
            minimum = Math.min(minimum, nums[mid]);

            /*
            * 4. Search Direction Logic:
            * We compare mid to left to see which "climb" we are currently on.
            */
            if (nums[mid] >= nums[left]) {
                /*
                * Case A: mid is part of the LEFT sorted portion (larger values).
                * Example: [4, 5, 6, 7, 0, 1, 2] -> mid is 7.
                * The true minimum must be to the right of mid.
                */
                left = mid + 1;
            } else {
                /*
                * Case B: mid is part of the RIGHT sorted portion (smaller values).
                * Example: [6, 7, 0, 1, 2, 4, 5] -> mid is 1.
                * The true minimum could be mid or to its left.
                */
                right = mid - 1;
            }
        }

        return minimum;
    }
}
