
package GoldmanSach.TrappingRainWater;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class TrappingRainWaterTest {

    private TrappingRainWater solution;

    @BeforeEach
    void setUp() {
        solution = new TrappingRainWater();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — standard case traps 9 units")
    void testLeetCodeExample1() {
        assertEquals(9, solution.trap(new int[] { 0, 1, 0, 2, 1, 0, 1, 3, 1, 0, 1, 2 }));
            // i=2: min(1,3)-0=1, i=4: min(2,3)-1=1, i=5: min(2,3)-0=2
           // i=6: min(2,3)-1=1, i=8: min(3,2)-1=1, i=9: min(3,2)-0=2, i=10: min(3,2)-1=1
           // total = 9
    }

    @Test
    @DisplayName("LC Example 2 — wide bars trap 9 units")
    void testLeetCodeExample2() {
        assertEquals(9, solution.trap(new int[]{4,2,0,3,2,5}));
    }

    // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns 0")
    void testNullInput() {
        assertEquals(0, solution.trap(null));
    }

    @Test
    @DisplayName("Edge 1b — empty array returns 0")
    void testEmptyArray() {
        assertEquals(0, solution.trap(new int[]{}));
    }

    // ─── EDGE CASE 2: fewer than 3 elements ───────────────────────────────

    @Test
    @DisplayName("Edge 2a — single element cannot trap water")
    void testSingleElement() {
        assertEquals(0, solution.trap(new int[]{5}));
    }

    @Test
    @DisplayName("Edge 2b — two elements cannot trap water")
    void testTwoElements() {
        assertEquals(0, solution.trap(new int[]{5, 3}));
    }

    // ─── EDGE CASE 3: no water trapped ────────────────────────────────────

    @Test
    @DisplayName("Edge 3a — strictly increasing, no water trapped")
    void testStrictlyIncreasing() {
        assertEquals(0, solution.trap(new int[]{1,2,3,4,5}));
    }

    @Test
    @DisplayName("Edge 3b — strictly decreasing, no water trapped")
    void testStrictlyDecreasing() {
        assertEquals(0, solution.trap(new int[]{5,4,3,2,1}));
    }

    @Test
    @DisplayName("Edge 3c — all equal heights, no water trapped")
    void testAllEqual() {
        assertEquals(0, solution.trap(new int[]{3,3,3,3,3}));
    }

    @Test
    @DisplayName("Edge 3d — valley at edges, no walls to trap water")
    void testValleyAtEdges() {
        assertEquals(0, solution.trap(new int[]{0,1,0}));
        // Left wall = 0, cannot trap anything
    }

    // ─── EDGE CASE 4: all zeros ────────────────────────────────────────────

    @Test
    @DisplayName("Edge 4 — all zeros, no water trapped")
    void testAllZeros() {
        assertEquals(0, solution.trap(new int[]{0,0,0,0,0}));
    }

    // ─── EDGE CASE 5: single valley ───────────────────────────────────────

    @Test
    @DisplayName("Edge 5a — simple valley traps 1 unit")
    void testSimpleValley() {
        assertEquals(1, solution.trap(new int[]{1,0,1}));
    }

    @Test
    @DisplayName("Edge 5b — deep valley bounded by equal walls")
    void testDeepValley() {
        assertEquals(3, solution.trap(new int[]{3,0,3}));
    }

    @Test
    @DisplayName("Edge 5c — valley bounded by unequal walls, shorter wall limits water")
    void testUnequalWalls() {
        assertEquals(2, solution.trap(new int[]{3,0,2}));
        // min(3,2) - 0 = 2
    }

    // ─── EDGE CASE 6: multiple valleys ────────────────────────────────────

    @Test
    @DisplayName("Edge 6 — multiple separate valleys")
    void testMultipleValleys() {
        assertEquals(4, solution.trap(new int[]{2,0,2,0,2}));
        // valley 1: min(2,2)-0 = 2, valley 2: min(2,2)-0 = 2 → total 4
    }

    // ─── EDGE CASE 7: plateau in the middle ───────────────────────────────

    @Test
    @DisplayName("Edge 7 — flat bottom valley traps water across plateau")
    void testFlatBottom() {
        assertEquals(6, solution.trap(new int[]{3,1,1,1,3}));
        // i=1: min(3,3)-1=2, i=2: min(3,3)-1=2, i=3: min(3,3)-1=2 → total=6
    }

    @Test
    @DisplayName("Edge 7b — flat bottom valley corrected")
    void testFlatBottomCorrected() {
        assertEquals(6, solution.trap(new int[]{3,1,1,1,3}));
    }

    // ─── EDGE CASE 8: peak in the middle ──────────────────────────────────

    @Test
    @DisplayName("Edge 8 — peak in the middle, water on both sides")
    void testPeakInMiddle() {
        assertEquals(2, solution.trap(new int[]{1,0,2,0,1}));
        // index 1: min(1,2)-0=1, index 3: min(2,1)-0=1 → total 2
    }

    // ─── EDGE CASE 9: water only on left side ─────────────────────────────

    @Test
    @DisplayName("Edge 9 — tall wall on right, water accumulates on left")
    void testTallRightWall() {
        assertEquals(2, solution.trap(new int[]{0,1,0,0,5}));
        // index 0: 0, index 1: 0 (it's a wall), index 2: min(1,5)-0=1
        // index 3: min(1,5)-0=1 → Hmm let me recalculate
        // leftMax:  [0,1,1,1,5]
        // rightMax: [5,5,5,5,5]
        // water: 0 + 0 + 1 + 1 + 0 = 2? No:
        // index 0: min(0,5)-0=0
        // index 1: min(1,5)-1=0
        // index 2: min(1,5)-0=1
        // index 3: min(1,5)-0=1
        // index 4: min(5,5)-5=0 → total = 2
    }

    @Test
    @DisplayName("Edge 9b — tall right wall corrected")
    void testTallRightWallCorrected() {
        assertEquals(2, solution.trap(new int[]{0,1,0,0,5}));
    }

    // ─── EDGE CASE 10: all same height except one dip ─────────────────────

    @Test
    @DisplayName("Edge 10 — one dip in a flat surface")
    void testOneDip() {
        assertEquals(2, solution.trap(new int[]{2,2,0,2,2}));
        // index 2: min(2,2)-0=2
    }

    // ─── EDGE CASE 11: staircase shapes ───────────────────────────────────

    @Test
    @DisplayName("Edge 11a — ascending staircase traps no water")
    void testAscendingStaircase() {
        assertEquals(0, solution.trap(new int[]{1,2,3,4,5,6}));
    }

    @Test
    @DisplayName("Edge 11b — descending staircase traps no water")
    void testDescendingStaircase() {
        assertEquals(0, solution.trap(new int[]{6,5,4,3,2,1}));
    }

    @Test
    @DisplayName("Edge 11c — mountain shape traps no water")
    void testMountainShape() {
        assertEquals(0, solution.trap(new int[]{1,2,3,4,3,2,1}));
    }

    // ─── EDGE CASE 12: large input ────────────────────────────────────────

    @Test
    @DisplayName("Edge 12 — large alternating array, O(n) solution required")
    void testLargeAlternatingInput() {
        int n = 100000;
        int[] height = new int[n];
        for (int i = 0; i < n; i++) {
            height[i] = (i % 2 == 0) ? 1 : 0;
        }
        // Every odd index traps min(1,1)-0 = 1 unit
        // Number of odd indices = n/2
        assertEquals(n / 2 - 1, solution.trap(height));
        // Last element is even index so last odd = n/2 - 1 pockets
    }

    // ─── EDGE CASE 13: two pointer correctness ────────────────────────────

    @Test
    @DisplayName("Edge 13 — asymmetric input validates two pointer logic")
    void testAsymmetricInput() {
        assertEquals(9, solution.trap(new int[]{4,2,0,3,2,5}));
        // i=1: min(4,5)-2=2, i=2: min(4,5)-0=4, i=3: min(4,5)-3=1, i=4: min(4,5)-2=2 → total=9
    }

    @Test
    @DisplayName("Edge 13b — left heavy input")
    void testLeftHeavyInput() {
        assertEquals(1, solution.trap(new int[]{5,4,1,2}));
        // leftMax:  [5,5,5,5]
        // rightMax: [5,4,2,2]
        // index 0: 0, index 1: min(5,4)-4=0
        // index 2: min(5,2)-1=1, index 3: min(5,2)-2=0 → total=1
        // Hmm, let me recalculate properly:
        // index 2: min(leftMax=5, rightMax=2) - height=1 = 1
        // total = 1
    }

    @Test
    @DisplayName("Edge 13c — verified asymmetric case")
    void testVerifiedAsymmetric() {
        assertEquals(1, solution.trap(new int[]{5,4,1,2}));
    }
}
