package GoldmanSach.ThreeSum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TC: O(n^2) — sort O(n log n) + two pointer O(n) per element
// SC: O(1) — excluding output list, sort is in-place
public class ThreeSum {

    public static void main(String[] args) {
        ThreeSum solution = new ThreeSum();
        System.out.println(solution.threeSum(new int[]{-1,0,1,2,-1,-4}));
        // [[-1,-1,2],[-1,0,1]]
    }


    public List<List<Integer>> threeSum(int[] nums) {
       List<List<Integer>> sum = new ArrayList<>();

       if (nums == null || nums.length < 3) return sum;

        // 1. Sort for ordering
        Arrays.sort(nums); // O(n log n)

        for (int index = 0; index < nums.length; index++) {
            int num = nums[index];

            // 2. We don't want to reuse the same value in the same position twice
            if (index > 0 && num == nums[index - 1]) {
                continue;
            }

            // 3. Two-sum logic with two pointers
            int left = index + 1;
            int right = nums.length - 1;

            while (left < right) {
                int threeSum = num + nums[left] + nums[right];

                if (threeSum > 0) {
                    right--;
                } else if (threeSum < 0) {
                    left++;
                } else {
                    // Found a triplet
                    sum.add(Arrays.asList(num, nums[left], nums[right]));
                    left++;

                    // 4. Skip duplicates for 'left' to avoid duplicate triplets
                    while (left < right && nums[left] == nums[left - 1]) {
                        left++;
                    }
                }
            }
        }
        return sum;
    }

}