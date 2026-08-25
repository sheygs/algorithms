package GoldmanSach.TrappingRainWater;


public class TrappingRainWater {
    public static void main(String[] args) {
        TrappingRainWater trapRainWater = new TrappingRainWater();
        int maxWater = trapRainWater.trap(new int[]{0,2,0,3,1,0,1,3,2,1});
        System.out.printf("Max Water Amount: ", maxWater);
    }

    /**
     * Two Pointer
     * Calculates the total rainwater trapped between bars after raining.
     * Time Complexity: O(N) - Single pass through the array.
     * Space Complexity: O(1) - Constant space used for pointers and max trackers.
     */
    public int trap(int[] height) {
        // 1. Validation: No bars means no water can be trapped
        if (height == null || height.length == 0) {
            return 0;
        }

        // 2. Initialization:
        // Pointers at the start and end of the array
        int left = 0, right = height.length - 1;

        // Tracks the highest bar seen so far from the left and right sides
        int leftMax = height[left], rightMax = height[right];
        int maxWaterAmount = 0;

        /*
        * 3. The Two-Pointer Logic:
        * We move the pointer that points to the shorter "max" wall.
        * Why? Because the amount of water trapped at any point is limited
        * by the shorter side (the bottleneck).
        */
        while (left < right) {
            if (leftMax < rightMax) {
                // Move left pointer in and update leftMax
                left++;
                leftMax = Math.max(leftMax, height[left]);

                // The water trapped is the difference between current max wall
                // and the height of the current bar.
                maxWaterAmount += leftMax - height[left];
            } else {
                // Move right pointer in and update rightMax
                right--;
                rightMax = Math.max(rightMax, height[right]);

                // If the right side is shorter or equal, it governs the water level here
                maxWaterAmount += rightMax - height[right];
            }
        }

        return maxWaterAmount;
    }
}
