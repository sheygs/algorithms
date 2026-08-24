package GoldmanSachs.ContainerWithMostWater;


public class ContainerWithMostWater {
   public static void main(String[] args) {
      ContainerWithMostWater containerWithMostWater = new ContainerWithMostWater();
      int maxArea = containerWithMostWater.maxArea(new int[]{10,1,1,1,1,10});
      System.out.println("MaxArea: " + maxArea);
   }

   // Time: O(n) - The left and right pointers start at opposite ends and move toward each other.
   // Space: O(1) - We are not using any additional data structures (like Maps or Lists) that scale with the input size
   // Two-Pointer
   public int maxArea(int[] heights) {
      // Basic edge case check: if array is null or empty, no container can be formed
      if (heights == null || heights.length == 0) {
         return 0;
      }

      // Initialize two pointers: 'left' at the start and 'right' at the end of the array
      int left = 0, right = heights.length - 1;
      int maxArea = 0;

      // Continue until the pointers meet
      while (left < right) {
         // Calculate the width (distance between pointers)
         // and height (limited by the shorter of the two lines)
         int area = (right - left) * Math.min(heights[left], heights[right]);

         // Update the global maximum if the current area is larger
         maxArea = Math.max(maxArea, area);

         // Strategy: Move the pointer pointing to the shorter line.
         // Since width is shrinking, we need a taller line to potentially increase the area.
         if (heights[left] < heights[right]) {
            left++; // Move left inward hoping for a taller bar
         } else if (heights[right] < heights[left]) {
            right--; // Move right inward hoping for a taller bar
         }
         else {
            // When heights are equal, moving either (or both) is fine
            // because neither can be a boundary for a larger area with the current width
            right--;
         }
      }

      return maxArea;
   }
}
