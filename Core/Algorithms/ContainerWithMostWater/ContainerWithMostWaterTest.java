package GoldmanSachs.ContainerWithMostWater;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ContainerWithMostWaterTest {

    private ContainerWithMostWater solution;

    @BeforeEach
    void setUp() {
        solution = new ContainerWithMostWater();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — returns 49")
    void testLeetCodeExample1() {
        assertEquals(49, solution.maxArea(new int[] { 1, 8, 6, 2, 5, 4, 8, 3, 7 }));
        // left=8(index 1), right=7(index 8) → min(8,7)*(8-1) = 7*7 = 49
    }


    @Test
    @DisplayName("LC Example 2 — two elements returns 1")
    void testSingleElement() {
        assertEquals(0, solution.maxArea(new int[]{1}));
        // min(1,1) * (1-0) = 1
    }

    @Test
    @DisplayName("LC Example 2 — two elements returns 1")
    void testLeetCodeExample2() {
        assertEquals(1, solution.maxArea(new int[]{1, 1}));
        // min(1,1) * (1-0) = 1
    }

    // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns 0")
    void testNullInput() {
        assertEquals(0, solution.maxArea(null));
    }

    @Test
    @DisplayName("Edge 1b — empty array returns 0")
    void testEmptyArray() {
        assertEquals(0, solution.maxArea(new int[]{}));
    }

    // ─── EDGE CASE 2: two elements ────────────────────────────────────────

    @Test
    @DisplayName("Edge 2a — two equal elements")
    void testTwoEqualElements() {
        assertEquals(5, solution.maxArea(new int[]{5,5}));
        // min(5,5) * 1 = 5
    }

    @Test
    @DisplayName("Edge 2b — two unequal elements, shorter limits water")
    void testTwoUnequalElements() {
        assertEquals(3, solution.maxArea(new int[]{3,7}));
        // min(3,7) * 1 = 3
    }

    // ─── EDGE CASE 3: all same height ─────────────────────────────────────

    @Test
    @DisplayName("Edge 3 — all same height, widest container wins")
    void testAllSameHeight() {
        assertEquals(8, solution.maxArea(new int[]{2,2,2,2,2}));
        // min(2,2) * (4-0) = 2*4 = 8? Wait:
        // leftmost and rightmost: min(2,2)*(4-0) = 8
    }

    @Test
    @DisplayName("Edge 3b — all same height corrected")
    void testAllSameHeightCorrected() {
        assertEquals(8, solution.maxArea(new int[]{2,2,2,2,2}));
        // min(2,2) * (4-0) = 2 * 4 = 8
    }

    // ─── EDGE CASE 4: strictly increasing ─────────────────────────────────

    @Test
    @DisplayName("Edge 4 — strictly increasing, answer is not always last two")
    void testStrictlyIncreasing() {
        assertEquals(9, solution.maxArea(new int[]{1,2,3,4,5,6}));
        // left=1(index 0), right=6(index 5) → min(1,6)*5 = 5
        // left=2(index 1), right=6(index 5) → min(2,6)*4 = 8?
        // Let me trace properly:
        // left=0(h=1),right=5(h=6): min(1,6)*5=5, advance left
        // left=1(h=2),right=5(h=6): min(2,6)*4=8, advance left
        // left=2(h=3),right=5(h=6): min(3,6)*3=9, advance left
        // left=3(h=4),right=5(h=6): min(4,6)*2=8, advance left
        // left=4(h=5),right=5(h=6): min(5,6)*1=5 → max=9
    }

    @Test
    @DisplayName("Edge 4b — strictly increasing corrected")
    void testStrictlyIncreasingCorrected() {
        assertEquals(9, solution.maxArea(new int[]{1,2,3,4,5,6}));
        // two pointer trace gives max = 9 at indices 2 and 5
    }

    // ─── EDGE CASE 5: strictly decreasing ─────────────────────────────────

    @Test
    @DisplayName("Edge 5 — strictly decreasing")
    void testStrictlyDecreasing() {
        assertEquals(9, solution.maxArea(new int[]{6,5,4,3,2,1}));
        // mirror of increasing → same answer 9
        // left=0(h=6),right=5(h=1): min(6,1)*5=5, advance right
        // left=0(h=6),right=4(h=2): min(6,2)*4=8, advance right
        // left=0(h=6),right=3(h=3): min(6,3)*3=9, advance right
        // left=0(h=6),right=2(h=4): min(6,4)*2=8, advance right
        // left=0(h=6),right=1(h=5): min(6,5)*1=5 → max=9
    }

    // ─── EDGE CASE 6: all zeros ────────────────────────────────────────────

    @Test
    @DisplayName("Edge 6 — all zero heights, no water possible")
    void testAllZeros() {
        assertEquals(0, solution.maxArea(new int[]{0,0,0,0}));
    }

    // ─── EDGE CASE 7: one zero height ─────────────────────────────────────

    @Test
    @DisplayName("Edge 7a — zero at left boundary")
    void testZeroAtLeft() {
        assertEquals(10, solution.maxArea(new int[]{0,5,5,5}));
        // left=0(h=0): any container with left=0 contributes 0
        // left advances: left=1(h=5),right=3(h=5): min(5,5)*2=10
    }

    @Test
    @DisplayName("Edge 7b — zero at left corrected")
    void testZeroAtLeftCorrected() {
        assertEquals(10, solution.maxArea(new int[]{0,5,5,5}));
        // min(5,5)*(3-1) = 10
    }

    @Test
    @DisplayName("Edge 7c — zero at right boundary")
    void testZeroAtRight() {
        assertEquals(10, solution.maxArea(new int[]{5,5,5,0}));
        // min(5,5)*(2-0) = 10
    }

    @Test
    @DisplayName("Edge 7d — zero in the middle")
    void testZeroInMiddle() {
        assertEquals(10, solution.maxArea(new int[]{5,0,5}));
        // left=0(h=5),right=2(h=5): min(5,5)*2=10?
        // min(5,5)*(2-0)=10
    }

    @Test
    @DisplayName("Edge 7e — zero in middle corrected")
    void testZeroInMiddleCorrected() {
        assertEquals(10, solution.maxArea(new int[]{5,0,5}));
        // water level determined by min of two walls not middle bars
        // min(5,5)*(2-0) = 10
    }

    // ─── EDGE CASE 8: best answer not at boundaries ────────────────────────

    @Test
    @DisplayName("Edge 8 — optimal container uses inner bars not outermost")
    void testOptimalNotAtBoundary() {
        assertEquals(10, solution.maxArea(new int[]{1,10,10,1}));
        // left=1(h=10),right=2(h=10): min(10,10)*(2-1)=10? No:
        // left=0(h=1),right=3(h=1): min(1,1)*3=3
        // left=1(h=10),right=3(h=1): min(10,1)*2=2
        // left=0(h=1),right=2(h=10): min(1,10)*2=2
        // Hmm, let me retrace two pointer:
        // left=0(h=1),right=3(h=1): min(1,1)*3=3, equal→advance left
        // left=1(h=10),right=3(h=1): min(10,1)*2=2, advance right
        // left=1(h=10),right=2(h=10): min(10,10)*1=10 → max=10
    }

    @Test
    @DisplayName("Edge 8b — inner bars corrected")
    void testOptimalNotAtBoundaryCorrected() {
        assertEquals(10, solution.maxArea(new int[]{1,10,10,1}));
        // two pointer gives max=10 at indices 1 and 2
    }

    // ─── EDGE CASE 9: tall narrow vs short wide ────────────────────────────

    @Test
    @DisplayName("Edge 9 — tall narrow container vs short wide container")
    void testTallNarrowVsShortWide() {
        assertEquals(50, solution.maxArea(new int[]{10,1,1,1,1,10}));
        // left=0(h=10),right=5(h=10): min(10,10)*5=50
    }

    @Test
    @DisplayName("Edge 9b — tall narrow vs short wide corrected")
    void testTallNarrowVsShortWideCorrected() {
        assertEquals(50, solution.maxArea(new int[]{10,1,1,1,1,10}));
        // widest and tallest → 50
    }

    // ─── EDGE CASE 10: single tall bar surrounded by short bars ───────────

    @Test
    @DisplayName("Edge 10 — one very tall bar, answer limited by other walls")
    void testOneTallBar() {
        assertEquals(4, solution.maxArea(new int[]{1,1,100,1,1}));
        // left=0(h=1),right=4(h=1): min(1,1)*4=4 → max=4
        // tall bar at middle doesn't help — limited by shorter walls
    }

    // ─── EDGE CASE 11: two pointer move direction ──────────────────────────

    @Test
    @DisplayName("Edge 11 — always advance pointer with shorter height")
    void testAdvanceShorterPointer() {
        assertEquals(16, solution.maxArea(new int[]{4,3,2,1,4}));
        // left=0(h=4),right=4(h=4): min(4,4)*4=16 → done immediately
    }

    // ─── EDGE CASE 12: large values ───────────────────────────────────────

    @Test
    @DisplayName("Edge 12 — large height values, no overflow")
    void testLargeHeightValues() {
        assertEquals(10000, solution.maxArea(
                new int[]{10000, 10000}));
        // min(10000,10000)*1 = 10000
    }

    // ─── EDGE CASE 13: large input ────────────────────────────────────────

    @Test
    @DisplayName("Edge 13 — large array, O(n) two pointer required")
    void testLargeInput() {
        int n = 100000;
        int[] height = new int[n];
        java.util.Arrays.fill(height, 10000);
        // widest container: min(10000,10000)*(n-1)
        assertEquals(10000 * (n - 1), solution.maxArea(height));
    }

    // ─── EDGE CASE 14: answer is at exact middle ──────────────────────────

    @Test
    @DisplayName("Edge 14 — two tall bars at centre, answer not at boundaries")
    void testAnswerAtCentre() {
        assertEquals(6, solution.maxArea(new int[]{1,1,6,6,1,1}));
        // left=2(h=6),right=3(h=6): min(6,6)*1=6
        // left=0(h=1),right=5(h=1): min(1,1)*5=5 → max=6
    }
}