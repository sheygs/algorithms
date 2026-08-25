package GoldmanSachs.BestTimeBuySellStock;

/**
 * Greedy Approach
 * Calculates the maximum profit possible from a single buy and sell transaction.
 * Time Complexity: O(N) — We only traverse the array once.
 * Space Complexity: O(1) — We use constant extra space regardless of input size.
 */
public class BestTimeBuySellStock {
   public static void main(String[] args) {
      BestTimeBuySellStock bestTimeBuySellStock = new BestTimeBuySellStock();
      int profit = bestTimeBuySellStock.maxProfit(new int[]{9,3,7,1,8,2});
      System.out.println("max profit: " + profit); // Expected: 7 (Buy at 1, Sell at 8)
   }

   public int maxProfit(int[] prices) {
      // 1. Validation: If there are no prices or only one price, no profit can be made.
      if (prices == null || prices.length < 2) {
         return 0;
      }

      int maxProfit = 0;
      // 2. Initialization: Set the initial minimum to the largest possible value
      // so the first price encountered will always replace it.
      int minimumPrice = Integer.MAX_VALUE;

      for (int price : prices) {
         /*
          * 3. Strategy:
          * We have two choices for every price we encounter:
          * Choice A: This is a new "all-time low." Update our buying price.
          * Choice B: This price is higher than our current low. Calculate potential profit.
          */
         if (price < minimumPrice) {
            // Found a cheaper day to buy
            minimumPrice = price;
         } else {
            // Check if selling today gives us a better profit than our previous record
            int currentProfit = price - minimumPrice;
            maxProfit = Math.max(maxProfit, currentProfit);
         }
      }

      return maxProfit;
   }
}