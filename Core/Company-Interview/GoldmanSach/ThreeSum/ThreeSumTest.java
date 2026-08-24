
package GoldmanSach.ThreeSum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ThreeSumTest {

    private ThreeSum solution;

    @BeforeEach
    void setUp() {
        solution = new ThreeSum();
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────
    // Normalise each triplet and the list for order-independent comparison
    private List<List<Integer>> normalise(List<List<Integer>> result) {
        result.forEach(triplet -> java.util.Collections.sort(triplet));
        result.sort((a, b) -> {
            for (int i = 0; i < 3; i++) {
                int cmp = Integer.compare(a.get(i), b.get(i));
                if (cmp != 0) return cmp;
            }
            return 0;
        });
        return result;
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — three unique triplets")
    void testLeetCodeExample1() {
        List<List<Integer>> result = normalise(
                solution.threeSum(new int[]{-1,0,1,2,-1,-4}));
        List<List<Integer>> expected = normalise(Arrays.asList(
                Arrays.asList(-1,-1,2),
                Arrays.asList(-1,0,1)
        ));
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("LC Example 2 — all zeros, one triplet")
    void testLeetCodeExample2() {
        List<List<Integer>> result = normalise(
                solution.threeSum(new int[]{0,0,0}));
        List<List<Integer>> expected = normalise(Arrays.asList(
                Arrays.asList(0,0,0)
        ));
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("LC Example 3 — no valid triplets")
    void testLeetCodeExample3() {
        List<List<Integer>> result = solution.threeSum(new int[]{0,1,1});
        assertTrue(result.isEmpty());
    }

    // ─── EDGE CASE 1: null / empty / too small ─────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns empty list")
    void testNullInput() {
        List<List<Integer>> result = solution.threeSum(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Edge 1b — empty array returns empty list")
    void testEmptyArray() {
        assertTrue(solution.threeSum(new int[]{}).isEmpty());
    }

    @Test
    @DisplayName("Edge 1c — fewer than 3 elements returns empty list")
    void testFewerThanThreeElements() {
        assertTrue(solution.threeSum(new int[]{1}).isEmpty());
        assertTrue(solution.threeSum(new int[]{1,2}).isEmpty());
    }

    // ─── EDGE CASE 2: all zeros ────────────────────────────────────────────

    @Test
    @DisplayName("Edge 2 — all zeros, only one triplet [0,0,0]")
    void testAllZeros() {
        List<List<Integer>> result = solution.threeSum(new int[]{0,0,0,0,0});
        assertEquals(1, result.size());
        assertEquals(Arrays.asList(0,0,0), result.get(0));
    }

    // ─── EDGE CASE 3: no valid triplets ───────────────────────────────────

    @Test
    @DisplayName("Edge 3a — all positive, no triplet sums to zero")
    void testAllPositive() {
        assertTrue(solution.threeSum(new int[]{1,2,3,4,5}).isEmpty());
    }

    @Test
    @DisplayName("Edge 3b — all negative, no triplet sums to zero")
    void testAllNegative() {
        assertTrue(solution.threeSum(new int[]{-5,-4,-3,-2,-1}).isEmpty());
    }

    @Test
    @DisplayName("Edge 3c — only two elements sum to zero, need third")
    void testOnlyTwoSumToZero() {
        assertTrue(solution.threeSum(new int[]{-1,1,2,3}).isEmpty());
        // -1+1=0 but no third 0 available
    }

    // ─── EDGE CASE 4: no duplicates in result ─────────────────────────────

    @Test
    @DisplayName("Edge 4 — duplicate numbers in input, no duplicate triplets in output")
    void testNoDuplicateTriplets() {
        List<List<Integer>> result = normalise(
                solution.threeSum(new int[]{-2,0,0,2,2}));
        assertEquals(1, result.size());
        assertEquals(Arrays.asList(-2,0,2), result.get(0));
        // [-2,0,2] appears only once despite multiple 0s and 2s
    }

    // ─── EDGE CASE 5: all same positive number ────────────────────────────

    @Test
    @DisplayName("Edge 5 — all same positive number, no triplets")
    void testAllSamePositive() {
        assertTrue(solution.threeSum(new int[]{3,3,3,3}).isEmpty());
        // 3+3+3 = 9 ≠ 0
    }

    // ─── EDGE CASE 6: all same negative number ────────────────────────────

    @Test
    @DisplayName("Edge 6 — all same negative number, no triplets")
    void testAllSameNegative() {
        assertTrue(solution.threeSum(new int[]{-3,-3,-3,-3}).isEmpty());
    }

    // ─── EDGE CASE 7: exactly three elements ──────────────────────────────

    @Test
    @DisplayName("Edge 7a — exactly three elements summing to zero")
    void testExactlyThreeElementsValid() {
        List<List<Integer>> result = solution.threeSum(new int[]{-1,0,1});
        assertEquals(1, result.size());
        assertEquals(Arrays.asList(-1,0,1), result.get(0));
    }

    @Test
    @DisplayName("Edge 7b — exactly three elements not summing to zero")
    void testExactlyThreeElementsInvalid() {
        assertTrue(solution.threeSum(new int[]{1,2,3}).isEmpty());
    }

    // ─── EDGE CASE 8: multiple valid triplets ─────────────────────────────

    @Test
    @DisplayName("Edge 8 — many valid triplets all returned")
    void testMultipleValidTriplets() {
        List<List<Integer>> result = normalise(
                solution.threeSum(new int[]{-4,-2,-2,-2,0,1,2,2,2,3,3,4,4,6,6}));
        assertFalse(result.isEmpty());
        // Verify no duplicates in result
        long distinctCount = result.stream().distinct().count();
        assertEquals(result.size(), distinctCount,
                "Result must not contain duplicate triplets");
    }

    // ─── EDGE CASE 9: contains zero ───────────────────────────────────────

    @Test
    @DisplayName("Edge 9 — array with multiple zeros")
    void testMultipleZeros() {
        List<List<Integer>> result = normalise(
                solution.threeSum(new int[]{-1,0,0,0,1}));
        List<List<Integer>> expected = normalise(Arrays.asList(
                Arrays.asList(-1,0,1),
                Arrays.asList(0,0,0)
        ));
        assertEquals(expected, result);
    }

    // ─── EDGE CASE 10: large values ───────────────────────────────────────

    @Test
    @DisplayName("Edge 10 — large positive and negative values")
    void testLargeValues() {
        List<List<Integer>> result = solution.threeSum(
                new int[]{-100000, 0, 100000});
        assertEquals(1, result.size());
        assertEquals(Arrays.asList(-100000, 0, 100000), result.get(0));
    }

    // ─── EDGE CASE 11: two pointer skip duplicates ────────────────────────

    @Test
    @DisplayName("Edge 11 — many duplicates, two pointer must skip correctly")
    void testSkipDuplicates() {
        List<List<Integer>> result = normalise(
                solution.threeSum(new int[]{-2,-2,-2,1,1,1,1}));
        assertEquals(1, result.size());
        assertEquals(Arrays.asList(-2,1,1), result.get(0));
        // [-2,1,1] once, not four times
    }

    // ─── EDGE CASE 12: negative + positive + zero ─────────────────────────

    @Test
    @DisplayName("Edge 12 — triplet must include zero as one element")
    void testTripletIncludesZero() {
        List<List<Integer>> result = normalise(
                solution.threeSum(new int[]{-3,0,3,-1,1}));
        List<List<Integer>> expected = normalise(Arrays.asList(
                Arrays.asList(-3,0,3),
                Arrays.asList(-1,0,1)
        ));
        assertEquals(expected, result);
    }

    // ─── EDGE CASE 13: large input ────────────────────────────────────────

    @Test
    @DisplayName("Edge 13 — large array, O(n^2) solution required")
    void testLargeInput() {
        int n = 3000;
        int[] nums = new int[n];
        // fill with alternating -1, 0, 1 → many [−1,0,1] triplets
        for (int i = 0; i < n; i++) {
            nums[i] = (i % 3) - 1; // -1, 0, 1, -1, 0, 1...
        }
        List<List<Integer>> result = solution.threeSum(nums);
        assertFalse(result.isEmpty());
        // just verify it completes and has no duplicates
        long distinctCount = result.stream().distinct().count();
        assertEquals(result.size(), distinctCount);
    }
}