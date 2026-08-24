package GoldmanSach.ProductOfArrayExceptSelf;

import java.util.Arrays;


public class ProductOfArrayExceptSelf {
   public static void main(String[] args) {
      ProductOfArrayExceptSelf productOfArrayExceptSelf = new ProductOfArrayExceptSelf();
      int[] result = productOfArrayExceptSelf.productExceptSelf(new int[] { -8, -4, -8 });
      System.out.println("result: "+Arrays.toString(result));
   }

   // prefix and suffix
   // TC: We perform three independent passes through the array (3n), which simplifies to linear time.
   // SC: we are creating two auxiliary arrays (prefix and suffix) of size 'n'
  public int[] productExceptSelf(int[] nums) {
      // Basic validation: return an empty array if input is null or empty
      if (nums == null || nums.length == 0) {
         return new int[]{};
      }

      int n = nums.length;

      // Arrays to store products of all elements to the left (prefix)
      // and all elements to the right (suffix) of each index
      int[] prefix = new int[n];
      int[] suffix = new int[n];
      int[] result = new int[n];

      // Pass 1: Build prefix products.
      // prefix[i] contains the product of all elements to the left of nums[i].
      prefix[0] = 1; // Nothing to the left of the first element
      for (int i = 1; i < n; i++) {
         prefix[i] = prefix[i - 1] * nums[i - 1];
      }

      // Pass 2: Build suffix products.
      // suffix[i] contains the product of all elements to the right of nums[i].
      suffix[n - 1] = 1; // Nothing to the right of the last element
      for (int i = n - 2; i >= 0; i--) {
         suffix[i] = suffix[i + 1] * nums[i + 1];
      }

      // Pass 3: Combine.
      // The product except self at index i is (everything to the left) * (everything to the right).
      for (int i = 0; i < n; i++) {
         result[i] = prefix[i] * suffix[i];
      }

      return result;
   }
}
