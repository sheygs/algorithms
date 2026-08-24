package GoldmanSachs.BestTimeBuySellStock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class BestTimeBuySellStockTest {

    private BestTimeBuySellStock solution;

    @BeforeEach
    void setUp() {
        solution = new BestTimeBuySellStock();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — buy on day 2 sell on day 5, profit = 5")
    void testLeetCodeExample1() {
        assertEquals(5, solution.maxProfit(new int[]{7,1,5,3,6,4}));
        // buy at 1, sell at 6 → profit = 5
    }

    @Test
    @DisplayName("LC Example 2 — prices always decreasing, no profit possible")
    void testLeetCodeExample2() {
        assertEquals(0, solution.maxProfit(new int[]{7,6,4,3,1}));
        // no profitable transaction → return 0
    }

    // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns 0")
    void testNullInput() {
        assertEquals(0, solution.maxProfit(null));
    }

    @Test
    @DisplayName("Edge 1b — empty array returns 0")
    void testEmptyArray() {
        assertEquals(0, solution.maxProfit(new int[]{}));
    }

    // ─── EDGE CASE 2: single element ──────────────────────────────────────

    @Test
    @DisplayName("Edge 2 — single price, cannot buy and sell, return 0")
    void testSingleElement() {
        assertEquals(0, solution.maxProfit(new int[]{5}));
    }

    // ─── EDGE CASE 3: two elements ────────────────────────────────────────

    @Test
    @DisplayName("Edge 3a — two elements increasing, profit is difference")
    void testTwoElementsProfit() {
        assertEquals(3, solution.maxProfit(new int[]{2,5}));
        // buy at 2, sell at 5 → profit = 3
    }

    @Test
    @DisplayName("Edge 3b — two elements decreasing, no profit")
    void testTwoElementsNoProfit() {
        assertEquals(0, solution.maxProfit(new int[]{5,2}));
    }

    @Test
    @DisplayName("Edge 3c — two elements equal, no profit")
    void testTwoElementsEqual() {
        assertEquals(0, solution.maxProfit(new int[]{3,3}));
    }

    // ─── EDGE CASE 4: all same price ──────────────────────────────────────

    @Test
    @DisplayName("Edge 4 — all prices equal, no profit possible")
    void testAllSamePrice() {
        assertEquals(0, solution.maxProfit(new int[]{5,5,5,5,5}));
    }

    // ─── EDGE CASE 5: strictly increasing ─────────────────────────────────

    @Test
    @DisplayName("Edge 5 — strictly increasing, buy first sell last")
    void testStrictlyIncreasing() {
        assertEquals(4, solution.maxProfit(new int[]{1,2,3,4,5}));
        // buy at 1, sell at 5 → profit = 4
    }

    // ─── EDGE CASE 6: strictly decreasing ─────────────────────────────────

    @Test
    @DisplayName("Edge 6 — strictly decreasing, no profit possible")
    void testStrictlyDecreasing() {
        assertEquals(0, solution.maxProfit(new int[]{5,4,3,2,1}));
    }

    // ─── EDGE CASE 7: best buy is not the first element ───────────────────

    @Test
    @DisplayName("Edge 7 — minimum price occurs in the middle, not at start")
    void testMinNotAtStart() {
        assertEquals(7, solution.maxProfit(new int[]{9,3,7,1,8,2}));
        // buy at 1 (index 3), sell at 8 (index 4) → profit = 7
        // Wait: min=1, max after min=8 → profit=7
    }

    @Test
    @DisplayName("Edge 7b — corrected, min in middle")
    void testMinNotAtStartCorrected() {
        assertEquals(7, solution.maxProfit(new int[]{9,3,7,1,8,2}));
        // buy at 1 (index 3), sell at 8 (index 4) → profit = 7
    }

    // ─── EDGE CASE 8: best sell is not the last element ───────────────────

    @Test
    @DisplayName("Edge 8 — maximum price occurs in the middle, not at end")
    void testMaxNotAtEnd() {
        assertEquals(5, solution.maxProfit(new int[]{1,6,3,2,1}));
        // buy at 1 (index 0), sell at 6 (index 1) → profit = 5
    }

    // ─── EDGE CASE 9: must buy before sell ────────────────────────────────

    @Test
    @DisplayName("Edge 9 — largest value comes before smallest, cannot use both")
    void testBuyBeforeSell() {
        assertEquals(1, solution.maxProfit(new int[]{10,1,2}));
        // cannot sell at 10 then buy at 1 — must buy first
        // buy at 1, sell at 2 → profit = 1
    }

    // ─── EDGE CASE 10: profit on last two elements only ───────────────────

    @Test
    @DisplayName("Edge 10 — only last two elements yield profit")
    void testProfitOnlyAtEnd() {
        assertEquals(3, solution.maxProfit(new int[]{5,4,3,2,1,4}));
        // buy at 1 (index 4), sell at 4 (index 5) → profit = 3
    }

    // ─── EDGE CASE 11: profit on first two elements only ──────────────────

    @Test
    @DisplayName("Edge 11 — only first two elements yield profit")
    void testProfitOnlyAtStart() {
        assertEquals(4, solution.maxProfit(new int[]{1,5,4,3,2,1}));
        // buy at 1 (index 0), sell at 5 (index 1) → profit = 4
    }

    // ─── EDGE CASE 12: valley then peak pattern ───────────────────────────

    @Test
    @DisplayName("Edge 12 — multiple valleys and peaks, pick global best")
    void testMultipleValleysAndPeaks() {
        assertEquals(9, solution.maxProfit(new int[]{3,1,9,2,10,4}));
        // buy at 1, sell at 10 → profit = 9? Wait:
        // min so far tracking: 3→1→1→1→1→1
        // profit at each: 0,0,8,1,9,3 → max = 9
    }

    @Test
    @DisplayName("Edge 12b — corrected valleys and peaks")
    void testMultipleValleysAndPeaksCorrected() {
        assertEquals(9, solution.maxProfit(new int[]{3,1,9,2,10,4}));
        // buy at 1 (index 1), sell at 10 (index 4) → profit = 9
    }

    // ─── EDGE CASE 13: large values ───────────────────────────────────────

    @Test
    @DisplayName("Edge 13a — Integer.MAX_VALUE price")
    void testMaxValuePrice() {
        assertEquals(Integer.MAX_VALUE - 1,
                solution.maxProfit(new int[]{1, Integer.MAX_VALUE}));
    }

    @Test
    @DisplayName("Edge 13b — prices near Integer.MAX_VALUE, no overflow")
    void testNearMaxValueNoOverflow() {
        assertEquals(1, solution.maxProfit(
                new int[]{Integer.MAX_VALUE - 1, Integer.MAX_VALUE}));
    }

    // ─── EDGE CASE 14: profit = 0 explicitly ──────────────────────────────

    @Test
    @DisplayName("Edge 14 — no profitable window anywhere, must return 0 not negative")
    void testReturnZeroNotNegative() {
        int result = solution.maxProfit(new int[]{9,8,7,6,5,4,3,2,1});
        assertTrue(result >= 0, "Result must never be negative");
        assertEquals(0, result);
    }

    // ─── EDGE CASE 15: large input ────────────────────────────────────────

    @Test
    @DisplayName("Edge 15 — large array, O(n) solution required")
    void testLargeInput() {
        int n = 100000;
        int[] prices = new int[n];
        for (int i = 0; i < n; i++) prices[i] = i + 1;
        // buy at 1, sell at n → profit = n - 1
        assertEquals(n - 1, solution.maxProfit(prices));
    }

    // ─── EDGE CASE 16: zigzag prices ──────────────────────────────────────

    @Test
    @DisplayName("Edge 16 — zigzag pattern, only one buy/sell allowed")
    void testZigzagPrices() {
        assertEquals(6, solution.maxProfit(new int[]{1,5,2,6,3,7}));
        // can only buy/sell once → buy at 1, sell at 7 → profit = 6?
        // min so far: 1,1,1,1,1,1
        // profit:     0,4,1,5,2,6 → max = 6
    }

    @Test
    @DisplayName("Edge 16b — corrected zigzag")
    void testZigzagPricesCorrected() {
        assertEquals(6, solution.maxProfit(new int[]{1,5,2,6,3,7}));
        // buy at 1 (index 0), sell at 7 (index 5) → profit = 6
    }
}