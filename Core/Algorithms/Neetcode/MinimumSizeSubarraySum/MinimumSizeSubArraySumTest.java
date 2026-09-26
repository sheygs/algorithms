package GoldmanSach.MinimumSizeSubArraySum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class MinimumSizeSubArraySumTest {

    private MinimumSizeSubArraySum solution;

    @BeforeEach
    void setUp() {
        solution = new MinimumSizeSubArraySum();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — subarray of length 2 is optimal")
    void testLeetCodeExample1() {
        assertEquals(2, solution.minSubArrayLen(7, new int[]{2,3,1,2,4,3}));
        // [4,3] = 7, length 2
    }

    @Test
    @DisplayName("LC Example 2 — entire array needed")
    void testLeetCodeExample2() {
        assertEquals(1, solution.minSubArrayLen(4, new int[]{1,4,4}));
        // [4] alone satisfies target
    }

    @Test
    @DisplayName("LC Example 3 — no valid subarray exists")
    void testLeetCodeExample3() {
        assertEquals(0, solution.minSubArrayLen(11, new int[]{1,1,1,1,1,1,1,1}));
        // sum of entire array = 8 < 11 → return 0
    }

    // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null array returns 0")
    void testNullInput() {
        assertEquals(0, solution.minSubArrayLen(5, null));
    }

    @Test
    @DisplayName("Edge 1b — empty array returns 0")
    void testEmptyArray() {
        assertEquals(0, solution.minSubArrayLen(5, new int[]{}));
    }

    // ─── EDGE CASE 2: single element ──────────────────────────────────────

    @Test
    @DisplayName("Edge 2a — single element equals target")
    void testSingleElementEqualsTarget() {
        assertEquals(1, solution.minSubArrayLen(5, new int[]{5}));
    }

    @Test
    @DisplayName("Edge 2b — single element exceeds target")
    void testSingleElementExceedsTarget() {
        assertEquals(1, solution.minSubArrayLen(5, new int[]{10}));
    }

    @Test
    @DisplayName("Edge 2c — single element below target")
    void testSingleElementBelowTarget() {
        assertEquals(0, solution.minSubArrayLen(5, new int[]{3}));
    }

    // ─── EDGE CASE 3: entire array required ───────────────────────────────

    @Test
    @DisplayName("Edge 3 — entire array is the only valid subarray")
    void testEntireArrayRequired() {
        assertEquals(5, solution.minSubArrayLen(15, new int[]{1,2,3,4,5}));
        // 1+2+3+4+5 = 15, only full array works
    }

    // ─── EDGE CASE 4: target exactly met (not exceeded) ───────────────────

    @Test
    @DisplayName("Edge 4 — subarray sum equals target exactly")
    void testSumEqualsTargetExactly() {
        assertEquals(2, solution.minSubArrayLen(9, new int[]{1,2,3,4,5}));
        // [4,5] = 9 exactly, length 2
    }

    // ─── EDGE CASE 5: answer is length 1 ──────────────────────────────────

    @Test
    @DisplayName("Edge 5 — single element satisfies, should return 1 not larger window")
    void testSingleElementSatisfies() {
        assertEquals(1, solution.minSubArrayLen(3, new int[]{1,1,5,1,1}));
        // [5] alone >= 3
    }

    // ─── EDGE CASE 6: all elements equal ──────────────────────────────────

    @Test
    @DisplayName("Edge 6 — all elements equal, minimal window")
    void testAllElementsEqual() {
        assertEquals(3, solution.minSubArrayLen(6, new int[]{2,2,2,2,2}));
        // need 3 twos to reach 6
    }

    // ─── EDGE CASE 7: sum just below target ───────────────────────────────

    @Test
    @DisplayName("Edge 7 — total sum is exactly 1 below target, return 0")
    void testSumJustBelowTarget() {
        assertEquals(0, solution.minSubArrayLen(16, new int[]{1,2,3,4,5}));
        // total = 15 < 16 → impossible
    }

    // ─── EDGE CASE 8: first element already satisfies ─────────────────────

    @Test
    @DisplayName("Edge 8 — first element alone satisfies target")
    void testFirstElementSatisfies() {
        assertEquals(1, solution.minSubArrayLen(3, new int[]{5,1,1,1,1}));
        // [5] >= 3, should shrink window immediately
    }

    // ─── EDGE CASE 9: last element alone satisfies ────────────────────────

    @Test
    @DisplayName("Edge 9 — last element alone satisfies target")
    void testLastElementSatisfies() {
        assertEquals(1, solution.minSubArrayLen(3, new int[]{1,1,1,1,5}));
        // [5] at end >= 3
    }

    // ─── EDGE CASE 10: target = 1 ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 10 — target is 1, any single element satisfies")
    void testTargetIsOne() {
        assertEquals(1, solution.minSubArrayLen(1, new int[]{3,2,1}));
    }

    // ─── EDGE CASE 11: large input ────────────────────────────────────────

    @Test
    @DisplayName("Edge 11 — large array, answer is 1")
    void testLargeArrayAnswerIsOne() {
        int[] nums = new int[100000];
        java.util.Arrays.fill(nums, 1);
        nums[50000] = 100000; // one element satisfies alone
        assertEquals(1, solution.minSubArrayLen(99999, nums));
    }

    // ─── EDGE CASE 12: all elements = 1, large target ─────────────────────

    @Test
    @DisplayName("Edge 12 — all ones, target equals array length")
    void testAllOnesTargetEqualsLength() {
        int[] nums = new int[]{1,1,1,1,1};
        assertEquals(5, solution.minSubArrayLen(5, nums));
        // Need all 5 elements
    }

    // ─── EDGE CASE 13: multiple subarrays of same min length ──────────────

    @Test
    @DisplayName("Edge 13 — multiple valid subarrays of same length, return that length")
    void testMultipleValidSubarraysOfSameLength() {
        assertEquals(2, solution.minSubArrayLen(6, new int[]{3,3,3,3}));
        // [3,3] works at index 0,1 and 1,2 and 2,3 — all length 2
    }
}