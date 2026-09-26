package GoldmanSach.StringCompression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class StringCompressionTest {

    private StringCompression solution;

    @BeforeEach
    void setUp() {
        solution = new StringCompression();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — mixed single and repeated chars")
    void testLeetCodeExample1() {
        char[] chars = {'a','a','b','b','c','c','c'};
        assertEquals(6, solution.compress(chars));
        assertArrayEquals(new char[]{'a','2','b','2','c','3'},
                          java.util.Arrays.copyOf(chars, 6));
        // a:2 → "a2", b:2 → "b2", c:3 → "c3"
    }

    @Test
    @DisplayName("LC Example 2 — single character, no compression")
    void testLeetCodeExample2() {
        char[] chars = {'a'};
        assertEquals(1, solution.compress(chars));
        assertArrayEquals(new char[]{'a'},
                          java.util.Arrays.copyOf(chars, 1));
    }

    @Test
    @DisplayName("LC Example 3 — count exceeds 9, multi-digit count")
    void testLeetCodeExample3() {
        char[] chars = {'a','b','b','b','b','b','b','b','b','b','b','b','b'};
        assertEquals(4, solution.compress(chars));
        assertArrayEquals(new char[]{'a','b','1','2'},
                          java.util.Arrays.copyOf(chars, 4));
        // a:1 → "a", b:12 → "b12"
    }

    // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns 0")
    void testNullInput() {
        assertEquals(0, solution.compress(null));
    }

    @Test
    @DisplayName("Edge 1b — empty array returns 0")
    void testEmptyArray() {
        assertEquals(0, solution.compress(new char[]{}));
    }

    // ─── EDGE CASE 2: all unique characters ───────────────────────────────

    @Test
    @DisplayName("Edge 2 — all unique chars, no compression possible")
    void testAllUniqueChars() {
        char[] chars = {'a','b','c','d'};
        assertEquals(4, solution.compress(chars));
        assertArrayEquals(new char[]{'a','b','c','d'},
                          java.util.Arrays.copyOf(chars, 4));
        // No char repeats so no counts written
    }

    // ─── EDGE CASE 3: all same character ──────────────────────────────────

    @Test
    @DisplayName("Edge 3 — all same character")
    void testAllSameChar() {
        char[] chars = {'a','a','a','a','a'};
        assertEquals(2, solution.compress(chars));
        assertArrayEquals(new char[]{'a','5'},
                          java.util.Arrays.copyOf(chars, 2));
    }

    // ─── EDGE CASE 4: count = 1 (no count written) ────────────────────────

    @Test
    @DisplayName("Edge 4 — single occurrence chars, count of 1 never written")
    void testSingleOccurrenceNoCountWritten() {
        char[] chars = {'a','b','c'};
        assertEquals(3, solution.compress(chars));
        assertArrayEquals(new char[]{'a','b','c'},
                          java.util.Arrays.copyOf(chars, 3));
        // Count of 1 must NOT be written per problem rules
    }

    // ─── EDGE CASE 5: count exactly 9 ─────────────────────────────────────

    @Test
    @DisplayName("Edge 5 — count exactly 9, single digit")
    void testCountExactlyNine() {
        char[] chars = {'a','a','a','a','a','a','a','a','a'};
        assertEquals(2, solution.compress(chars));
        assertArrayEquals(new char[]{'a','9'},
                          java.util.Arrays.copyOf(chars, 2));
    }

    // ─── EDGE CASE 6: count exactly 10 ────────────────────────────────────

    @Test
    @DisplayName("Edge 6 — count exactly 10, two digit count")
    void testCountExactlyTen() {
        char[] chars = {'a','a','a','a','a','a','a','a','a','a'};
        assertEquals(3, solution.compress(chars));
        assertArrayEquals(new char[]{'a','1','0'},
                          java.util.Arrays.copyOf(chars, 3));
    }

    // ─── EDGE CASE 7: count exactly 100 ───────────────────────────────────

    @Test
    @DisplayName("Edge 7 — count exactly 100, three digit count")
    void testCountExactlyHundred() {
        char[] chars = new char[100];
        java.util.Arrays.fill(chars, 'a');
        assertEquals(4, solution.compress(chars));
        assertArrayEquals(new char[]{'a','1','0','0'},
                          java.util.Arrays.copyOf(chars, 4));
    }

    // ─── EDGE CASE 8: alternating characters ──────────────────────────────

    @Test
    @DisplayName("Edge 8 — alternating chars, no compression possible")
    void testAlternatingChars() {
        char[] chars = {'a','b','a','b','a','b'};
        assertEquals(6, solution.compress(chars));
        assertArrayEquals(new char[]{'a','b','a','b','a','b'},
                          java.util.Arrays.copyOf(chars, 6));
        // Every run is length 1, no counts written
    }

    // ─── EDGE CASE 9: two groups only ─────────────────────────────────────

    @Test
    @DisplayName("Edge 9 — exactly two groups")
    void testTwoGroups() {
        char[] chars = {'a','a','b','b','b'};
        assertEquals(4, solution.compress(chars));
        assertArrayEquals(new char[]{'a','2','b','3'},
                          java.util.Arrays.copyOf(chars, 4));
    }

    // ─── EDGE CASE 10: mixed single and multi counts ───────────────────────

    @Test
    @DisplayName("Edge 10 — mix of single chars and repeated groups")
    void testMixedSingleAndMulti() {
        char[] chars = {'a','b','b','c','c','c','d'};
        assertEquals(6, solution.compress(chars));
        assertArrayEquals(new char[]{'a','b','2','c','3','d'},
                          java.util.Arrays.copyOf(chars, 6));
        // a:1 → "a", b:2 → "b2", c:3 → "c3", d:1 → "d"
    }

    // ─── EDGE CASE 11: in-place check ─────────────────────────────────────

    @Test
    @DisplayName("Edge 11 — result must be written in-place to original array")
    void testInPlaceModification() {
        char[] chars = {'a','a','a'};
        int len = solution.compress(chars);
        assertEquals(2, len);
        assertEquals('a', chars[0]);
        assertEquals('3', chars[1]);
        // Must use the SAME array, not a new one
    }

    // ─── EDGE CASE 12: return value is new length ──────────────────────────

    @Test
    @DisplayName("Edge 12 — return value is new length not original length")
    void testReturnValueIsNewLength() {
        char[] chars = {'a','a','a','a','a','a'};
        int originalLength = chars.length; // 6
        int newLength = solution.compress(chars);
        assertEquals(2, newLength);
        assertNotEquals(originalLength, newLength,
                "Must return compressed length, not original length");
    }

    // ─── EDGE CASE 13: large input, count = 2000 ──────────────────────────

    @Test
    @DisplayName("Edge 13 — very large group, 4 digit count")
    void testVeryLargeGroup() {
        char[] chars = new char[2000];
        java.util.Arrays.fill(chars, 'z');
        assertEquals(5, solution.compress(chars));
        assertArrayEquals(new char[]{'z','2','0','0','0'},
                          java.util.Arrays.copyOf(chars, 5));
    }
}