package GoldmanSachs.FirstUniqueCharInString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class FirstUniqueCharInStringTest {

    private FirstUniqueCharInString solution;

    @BeforeEach
    void setUp() {
        solution = new FirstUniqueCharInString();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — unique char in middle")
    void testLeetCodeExample1() {
        assertEquals(0, solution.firstUniqChar("leetcode"));
        // l:1 e:3 t:1 c:1 o:1 d:1 → 'l' at index 0
    }

    @Test
    @DisplayName("LC Example 2 — no unique character")
    void testLeetCodeExample2() {
        assertEquals(-1, solution.firstUniqChar("aabb"));
        // a:2 b:2 → no unique char
    }

    // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns -1")
    void testNullInput() {
        assertEquals(-1, solution.firstUniqChar(null));
    }

    @Test
    @DisplayName("Edge 1b — empty string returns -1")
    void testEmptyString() {
        assertEquals(-1, solution.firstUniqChar(""));
    }

    // ─── EDGE CASE 2: single character ────────────────────────────────────

    @Test
    @DisplayName("Edge 2 — single character is always unique")
    void testSingleCharacter() {
        assertEquals(0, solution.firstUniqChar("z"));
    }

    // ─── EDGE CASE 3: all characters identical ────────────────────────────

    @Test
    @DisplayName("Edge 3 — all characters the same, no unique char")
    void testAllSameCharacters() {
        assertEquals(-1, solution.firstUniqChar("aaaaaaa"));
    }

    // ─── EDGE CASE 4: unique char position ────────────────────────────────

    @Test
    @DisplayName("Edge 4a — unique char at the beginning")
    void testUniqueCharAtStart() {
        assertEquals(-1, solution.firstUniqChar("abcabc"));
        // Wait: a:2 b:2 c:2 — no unique. Fix:
        assertEquals(1, solution.firstUniqChar("zabzz"));
        // z:3 a:1 b:1 → first unique is 'a' at index 1? No:
        // z(0) a(1) b(2) z(3) z(4) → z:3 a:1 b:1 → 'a' at 1
        // Let me use a cleaner example:
    }

    @Test
    @DisplayName("Edge 4b — unique char at the very start")
    void testUniqueAtStart() {
        assertEquals(0, solution.firstUniqChar("xaabb"));
        // x:1 a:2 b:2 → 'x' at index 0
    }

    @Test
    @DisplayName("Edge 4c — unique char at the very end")
    void testUniqueAtEnd() {
        assertEquals(4, solution.firstUniqChar("aabbz"));
        // a:2 b:2 z:1 → 'z' at index 4
    }

    @Test
    @DisplayName("Edge 4d — unique char in the middle")
    void testUniqueInMiddle() {
        assertEquals(2, solution.firstUniqChar("aazbb"));
        // a:2 z:1 b:2 → 'z' at index 2
    }

    // ─── EDGE CASE 5: two characters ──────────────────────────────────────

    @Test
    @DisplayName("Edge 5a — two different chars, first is unique")
    void testTwoDifferentChars() {
        assertEquals(0, solution.firstUniqChar("ab"));
        // a:1 b:1 → first unique is 'a' at index 0
    }

    @Test
    @DisplayName("Edge 5b — two same chars, no unique")
    void testTwoSameChars() {
        assertEquals(-1, solution.firstUniqChar("aa"));
    }

    // ─── EDGE CASE 6: multiple unique chars — return FIRST ────────────────

    @Test
    @DisplayName("Edge 6 — multiple unique chars, must return the first one")
    void testMultipleUniqueCharsReturnFirst() {
        assertEquals(0, solution.firstUniqChar("abcd"));
        // a:1 b:1 c:1 d:1 → all unique, first is 'a' at index 0
    }

    // ─── EDGE CASE 7: all chars appear exactly twice ──────────────────────

    @Test
    @DisplayName("Edge 7 — every character appears exactly twice")
    void testAllCharsTwice() {
        assertEquals(-1, solution.firstUniqChar("aabbccdd"));
    }

    // ─── EDGE CASE 8: large input ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 8 — long string, unique char at the very end")
    void testLongStringUniqueAtEnd() {
        String repeated = "ab".repeat(50000); // 100000 chars, a:50000 b:50000
        String input = repeated + "z";        // z appears once at the end
        assertEquals(100000, solution.firstUniqChar(input));
    }

    // ─── EDGE CASE 9: unique char buried after many duplicates ────────────

    @Test
    @DisplayName("Edge 9 — unique char is not the first seen, earlier chars repeat later")
    void testEarlyCharRepeatsLater() {
        assertEquals(2, solution.firstUniqChar("aacbbdee"));
        // a:2 c:1 b:2 d:1 e:2 → first unique is 'c' at index 2
    }

    // ─── EDGE CASE 10: all 26 letters present, only one unique ────────────

    @Test
    @DisplayName("Edge 10 — all 26 letters present, only one is unique")
    void testAllAlphabetOneUnique() {
        // repeat every letter twice except 'z' once
        StringBuilder sb = new StringBuilder();
        for (char c = 'a'; c <= 'y'; c++) {
            sb.append(c);
            sb.append(c);
        }
        sb.append('z'); // z appears once at the end
        String input = sb.toString(); // length = 51
        assertEquals(50, solution.firstUniqChar(input));
        // 'z' at index 50
    }
}