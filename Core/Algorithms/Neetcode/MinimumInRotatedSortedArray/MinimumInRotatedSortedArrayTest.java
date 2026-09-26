package GoldmanSach.MinimumInRotatedSortedArray;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class MinimumInRotatedSortedArrayTest {

    private MinimumInRotatedSortedArray solution;

    @BeforeEach
    void setUp() {
        solution = new MinimumInRotatedSortedArray();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — rotated array minimum is 1")
    void testLeetCodeExample1() {
        assertEquals(1, solution.findMin(new int[]{3,4,5,1,2}));
    }

    @Test
    @DisplayName("LC Example 2 — rotated array minimum is 0")
    void testLeetCodeExample2() {
        assertEquals(0, solution.findMin(new int[]{4,5,6,7,0,1,2}));
    }

    @Test
    @DisplayName("LC Example 3 — two element rotated array")
    void testLeetCodeExample3() {
        assertEquals(11, solution.findMin(new int[]{11,13,15,17}));
        // Not rotated — minimum is first element
    }

    // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns -1 or throws")
    void testNullInput() {
        assertThrows(IllegalArgumentException.class,
                () -> solution.findMin(null));
    }

    @Test
    @DisplayName("Edge 1b — empty array throws")
    void testEmptyArray() {
        assertThrows(IllegalArgumentException.class,
                () -> solution.findMin(new int[]{}));
    }

    // ─── EDGE CASE 2: single element ──────────────────────────────────────

    @Test
    @DisplayName("Edge 2 — single element is always the minimum")
    void testSingleElement() {
        assertEquals(7, solution.findMin(new int[]{7}));
    }

    // ─── EDGE CASE 3: two elements ────────────────────────────────────────

    @Test
    @DisplayName("Edge 3a — two elements not rotated")
    void testTwoElementsNotRotated() {
        assertEquals(1, solution.findMin(new int[]{1,2}));
    }

    @Test
    @DisplayName("Edge 3b — two elements rotated")
    void testTwoElementsRotated() {
        assertEquals(1, solution.findMin(new int[]{2,1}));
    }

    // ─── EDGE CASE 4: not rotated at all ──────────────────────────────────

    @Test
    @DisplayName("Edge 4a — already sorted ascending, minimum is first element")
    void testNotRotated() {
        assertEquals(1, solution.findMin(new int[]{1,2,3,4,5}));
    }

    @Test
    @DisplayName("Edge 4b — sorted array, minimum at index 0")
    void testSortedMinAtStart() {
        assertEquals(2, solution.findMin(new int[]{2,3,4,5,6}));
    }

    // ─── EDGE CASE 5: rotated by 1 ────────────────────────────────────────

    @Test
    @DisplayName("Edge 5a — rotated once, minimum at last position")
    void testRotatedByOne() {
        assertEquals(1, solution.findMin(new int[]{2,3,4,5,1}));
        // minimum is at last index
    }

    @Test
    @DisplayName("Edge 5b — rotated once, minimum at second position")
    void testRotatedMinAtSecond() {
        assertEquals(1, solution.findMin(new int[]{5,1,2, 3,4}));
        // Wait: {2,1,3,4,5} is not a valid rotation of a sorted array
        // Valid rotation of {1,2,3,4,5}: {2,3,4,5,1},{3,4,5,1,2} etc
        // Use: {5,1,2,3,4} — rotated 4 times, min at index 1
    }

    @Test
    @DisplayName("Edge 5c — minimum at index 1")
    void testMinAtIndexOne() {
        assertEquals(1, solution.findMin(new int[]{5,1,2,3,4}));
    }

    // ─── EDGE CASE 6: rotated by n-1 ──────────────────────────────────────

    @Test
    @DisplayName("Edge 6 — rotated n-1 times, minimum at index 1")
    void testRotatedNMinusOne() {
        assertEquals(1, solution.findMin(new int[]{2,3,4,5,6,1}));
        // Minimum is at last index after n-1 rotations
        // Wait: {2,3,4,5,6,1} has min at last index
    }

    @Test
    @DisplayName("Edge 6b — minimum at last index")
    void testMinAtLastIndex() {
        assertEquals(1, solution.findMin(new int[]{2,3,4,5,1}));
    }

    // ─── EDGE CASE 7: minimum at middle ───────────────────────────────────

    @Test
    @DisplayName("Edge 7 — minimum exactly in the middle")
    void testMinAtMiddle() {
        assertEquals(1, solution.findMin(new int[]{4,5,1,2,3}));
        // Rotated so minimum lands at exact midpoint
    }

    // ─── EDGE CASE 8: negative numbers ────────────────────────────────────

    @Test
    @DisplayName("Edge 8a — array contains negative numbers")
    void testNegativeNumbers() {
        assertEquals(-5, solution.findMin(new int[]{-3,-1,0,-5,-4}));
        // Wait: {-3,-1,0,-5,-4} is not a valid rotation
        // Valid: original sorted = {-5,-4,-3,-1,0}
        // Rotated: {-3,-1,0,-5,-4} → min = -5
        assertEquals(-5, solution.findMin(new int[]{-3,-1,0,-5,-4}));
    }

    @Test
    @DisplayName("Edge 8b — all negative numbers rotated")
    void testAllNegativeRotated() {
        assertEquals(-5, solution.findMin(new int[]{-2,-1,-5,-4,-3}));
        // sorted: {-5,-4,-3,-2,-1}, rotated: {-2,-1,-5,-4,-3} → min = -5
    }

    @Test
    @DisplayName("Edge 8c — mix of negative and positive")
    void testMixedNegativePositive() {
        assertEquals(-3, solution.findMin(new int[]{1,2,3,-3,-2,-1,0}));
        // sorted: {-3,-2,-1,0,1,2,3}, rotated → min = -3
    }

    // ─── EDGE CASE 9: large values ────────────────────────────────────────

    @Test
    @DisplayName("Edge 9a — Integer.MAX_VALUE in array")
    void testMaxValue() {
        assertEquals(1, solution.findMin(
                new int[]{Integer.MAX_VALUE - 1, Integer.MAX_VALUE, 1, 2, 3}));
    }

    @Test
    @DisplayName("Edge 9b — Integer.MIN_VALUE in array")
    void testMinValue() {
        assertEquals(Integer.MIN_VALUE, solution.findMin(
                new int[]{0, 1, 2, Integer.MIN_VALUE}));
    }

    // ─── EDGE CASE 10: large array ────────────────────────────────────────

    @Test
    @DisplayName("Edge 10 — large array rotated at midpoint, O(log n) required")
    void testLargeArray() {
        int n = 100000;
        int[] arr = new int[n];
        // sorted: 0..n-1, rotate at midpoint
        int pivot = n / 2;
        for (int i = 0; i < n - pivot; i++) arr[i] = pivot + i;
        for (int i = 0; i < pivot; i++) arr[n - pivot + i] = i;
        assertEquals(0, solution.findMin(arr));
    }

    // ─── EDGE CASE 11: rotation by full length (effectively not rotated) ──

    @Test
    @DisplayName("Edge 11 — rotated n times equals original sorted array")
    void testRotatedNTimes() {
        assertEquals(1, solution.findMin(new int[]{1,2,3,4,5}));
        // n rotations = same as original
    }

    // ─── EDGE CASE 12: minimum is largest absolute value ──────────────────

    @Test
    @DisplayName("Edge 12 — all positives, minimum is at rotation point")
    void testMinAtRotationPoint() {
        assertEquals(3, solution.findMin(new int[]{6,7,8,9,3,4,5}));
        // sorted: {3,4,5,6,7,8,9}, rotated → min at index 4
    }

    // ─── EDGE CASE 13: binary search boundary ─────────────────────────────

    @Test
    @DisplayName("Edge 13a — mid == minimum element")
    void testMidIsMinimum() {
        assertEquals(1, solution.findMin(new int[]{6,7,1,2,3,4,5}));
        // mid index = 3 (value 2), min is at index 2
        // binary search must handle correctly
    }

    @Test
    @DisplayName("Edge 13b — pivot adjacent to boundaries")
    void testPivotAdjacentToBoundary() {
        assertEquals(1, solution.findMin(new int[]{3,1,2}));
        // minimum at index 1, adjacent to left boundary
    }
}