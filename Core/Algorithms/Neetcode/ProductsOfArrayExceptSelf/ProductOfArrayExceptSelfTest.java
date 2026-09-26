package GoldmanSach.ProductOfArrayExceptSelf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ProductOfArrayExceptSelfTest {

    private ProductOfArrayExceptSelf solution;

    @BeforeEach
    void setUp() {
        solution = new ProductOfArrayExceptSelf();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — standard case")
    void testLeetCodeExample1() {
        assertArrayEquals(new int[]{24,12,8,6},
                solution.productExceptSelf(new int[]{1,2,3,4}));
        // index 0: 2*3*4=24, index 1: 1*3*4=12
        // index 2: 1*2*4=8,  index 3: 1*2*3=6
    }

    @Test
    @DisplayName("LC Example 2 — array with zero")
    void testLeetCodeExample2() {
        assertArrayEquals(new int[]{0,0,9,0,0},
                solution.productExceptSelf(new int[]{-1,1,0,-3,3}));
        // index 2 is the only non-zero: (-1)*1*(-3)*3=9
        // all others contain 0 in their product → 0
    }

    // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns empty array")
    void testNullInput() {
        assertArrayEquals(new int[]{}, solution.productExceptSelf(null));
    }

    @Test
    @DisplayName("Edge 1b — empty array returns empty array")
    void testEmptyArray() {
        assertArrayEquals(new int[]{}, solution.productExceptSelf(new int[]{}));
    }

    // ─── EDGE CASE 2: single element ──────────────────────────────────────

    @Test
    @DisplayName("Edge 2 — single element, product of empty set is 1")
    void testSingleElement() {
        assertArrayEquals(new int[]{1},
                solution.productExceptSelf(new int[]{5}));
    }

    // ─── EDGE CASE 3: two elements ────────────────────────────────────────

    @Test
    @DisplayName("Edge 3 — two elements swap each other")
    void testTwoElements() {
        assertArrayEquals(new int[]{3,2},
                solution.productExceptSelf(new int[]{2,3}));
    }

    // ─── EDGE CASE 4: array contains one zero ─────────────────────────────

    @Test
    @DisplayName("Edge 4 — exactly one zero, only that index gets non-zero result")
    void testOneZero() {
        assertArrayEquals(new int[]{0,0,6,0},
                solution.productExceptSelf(new int[]{1,2,0,3}));
        // index 2: 1*2*3=6, all others contain the zero → 0
    }

    // ─── EDGE CASE 5: array contains two zeros ────────────────────────────

    @Test
    @DisplayName("Edge 5 — two zeros, all products are zero")
    void testTwoZeros() {
        assertArrayEquals(new int[]{0,0,0,0},
                solution.productExceptSelf(new int[]{1,0,2,0}));
    }

    // ─── EDGE CASE 6: all zeros ────────────────────────────────────────────

    @Test
    @DisplayName("Edge 6 — all zeros, result is all zeros")
    void testAllZeros() {
        assertArrayEquals(new int[]{0,0,0,0},
                solution.productExceptSelf(new int[]{0,0,0,0}));
    }

    // ─── EDGE CASE 7: all ones ─────────────────────────────────────────────

    @Test
    @DisplayName("Edge 7 — all ones, every product is 1")
    void testAllOnes() {
        assertArrayEquals(new int[]{1,1,1,1},
                solution.productExceptSelf(new int[]{1,1,1,1}));
    }

    // ─── EDGE CASE 8: negative numbers ────────────────────────────────────

    @Test
    @DisplayName("Edge 8a — all negative numbers, pairs give positive results")
    void testAllNegative() {
        assertArrayEquals(new int[]{8,4,2},
                solution.productExceptSelf(new int[]{-1,-2,-4}));
        // index 0: (-2)*(-4) = 8
        // index 1: (-1)*(-4) = 4
        // index 2: (-1)*(-2) = 2
    }

    @Test
    @DisplayName("Edge 8b — mix of negative and positive")
    void testMixedSigns() {
        assertArrayEquals(new int[]{-6,6,-3,2},
                solution.productExceptSelf(new int[]{-1,1,-2,3}));
        // index 0: 1*(-2)*3    = -6
        // index 1: (-1)*(-2)*3 =  6
        // index 2: (-1)*1*3    = -3
        // index 3: (-1)*1*(-2) =  2
    }

    // ─── EDGE CASE 9: contains ones ───────────────────────────────────────

    @Test
    @DisplayName("Edge 9 — array with 1s, ones do not affect product")
    void testArrayWithOnes() {
        assertArrayEquals(new int[]{6,6,2,3},
                solution.productExceptSelf(new int[]{1,1,3,2}));
        // index 0: 1*3*2=6, index 1: 1*3*2=6
        // index 2: 1*1*2=2, index 3: 1*1*3=3
    }

    // ─── EDGE CASE 10: overflow check ─────────────────────────────────────

    @Test
    @DisplayName("Edge 10 — overflow at indices 0 and 1, indices 2 and 3 unaffected")
    void testOverflowRisk() {
        int[] result = solution.productExceptSelf(new int[]{1,1,50000,50000});
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals(50000, result[2]); // prefix[2]=1,     suffix[2]=50000 → 50000
        assertEquals(50000, result[3]); // prefix[3]=50000, suffix[3]=1     → 50000
        // indices 0 and 1: 50000*50000=2.5B overflows int — just verify no crash
        assertNotNull(result[0]);
        assertNotNull(result[1]);
    }

    // ─── EDGE CASE 11: no division — handles zero correctly ───────────────

    @Test
    @DisplayName("Edge 11 — two zeros, all products zero, no division by zero")
    void testNoDivisionApproach() {
        assertArrayEquals(new int[]{0,0,0,0},
                solution.productExceptSelf(new int[]{1,0,0,2}));
        // two zeros means every product includes at least one zero
    }

    // ─── EDGE CASE 12: large input ────────────────────────────────────────

    @Test
    @DisplayName("Edge 12 — large array all ones, O(n) required")
    void testLargeInputAllOnes() {
        int n = 100000;
        int[] input = new int[n];
        java.util.Arrays.fill(input, 1);
        int[] expected = new int[n];
        java.util.Arrays.fill(expected, 1);
        assertArrayEquals(expected, solution.productExceptSelf(input));
    }

    // ─── EDGE CASE 13: output length ──────────────────────────────────────

    @Test
    @DisplayName("Edge 13 — output length must equal input length")
    void testOutputLengthEqualsInput() {
        int[] input = {1,2,3,4,5};
        int[] result = solution.productExceptSelf(input);
        assertEquals(input.length, result.length,
                "Output array length must equal input array length");
    }
}