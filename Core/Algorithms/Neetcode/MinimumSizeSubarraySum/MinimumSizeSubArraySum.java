package GoldmanSach.MinimumSizeSubArraySum;

public class MinimumSizeSubArraySum {
    /**
     * Sliding Window
     * Finds the minimal length of a contiguous subarray of which the sum is >= target.
     * Time Complexity: O(N) - Each element is visited at most twice (once by 'right', once by 'left').
     * Space Complexity: O(1) - Only a few integer variables are used.
     */
    public int minSubArrayLen(int target, int[] nums) {
        // 1. Validation: Handle empty or null arrays
        if (nums == null || nums.length == 0) {
            return 0;
        }

        // 2. Initialization:
        // We set minimumLength to MAX_VALUE so that any valid subarray found will be smaller.
        int minimumLength = Integer.MAX_VALUE;
        int left = 0;   // The 'start' of our sliding window
        int total = 0;  // Keeps track of the sum of the current window

        /*
            * 3. The Sliding Window Strategy:
            * The 'right' pointer expands the window by adding elements to the total.
        */
        for (int right = 0; right < nums.length; right++) {
            total += nums[right];

            /*
                * 4. Shrinking Phase:
                * While the current window's sum meets or exceeds the target, we try to
                * shrink it from the left to find a smaller possible length.
            */
            while (total >= target) {
                // Update the global minimum length found so far
                minimumLength = Math.min(minimumLength, right - left + 1);

                // Subtract the element at 'left' and move the pointer forward
                total -= nums[left];
                left++;
            }
        }

        // 5. Result:
        // If minimumLength was never updated, it means no valid subarray was found.
        return minimumLength == Integer.MAX_VALUE ? 0 : minimumLength;
    }
}
