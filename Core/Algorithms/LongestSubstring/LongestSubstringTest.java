package GoldmanSachs.LongestSubstring;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class LongestSubstringTest {

    private LongestSubstring solution;

    @BeforeEach
    void setUp() {
        solution = new LongestSubstring();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — abcabcbb returns 3")
    void testLeetCodeExample1() {
        assertEquals(3, solution.lengthOfLongestSubstring("abcabcbb"));
        // "abc" = 3
    }

    @Test
    @DisplayName("LC Example 2 — bbbbb returns 1")
    void testLeetCodeExample2() {
        assertEquals(1, solution.lengthOfLongestSubstring("bbbbb"));
        // "b" = 1
    }

    @Test
    @DisplayName("LC Example 3 — pwwkew returns 3")
    void testLeetCodeExample3() {
        assertEquals(3, solution.lengthOfLongestSubstring("pwwkew"));
        // "wke" = 3
    }

    // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns 0")
    void testNullInput() {
        assertEquals(0, solution.lengthOfLongestSubstring(null));
    }

    @Test
    @DisplayName("Edge 1b — empty string returns 0")
    void testEmptyString() {
        assertEquals(0, solution.lengthOfLongestSubstring(""));
    }

    // ─── EDGE CASE 2: single character ────────────────────────────────────

    @Test
    @DisplayName("Edge 2 — single character always returns 1")
    void testSingleCharacter() {
        assertEquals(1, solution.lengthOfLongestSubstring("a"));
    }

    // ─── EDGE CASE 3: all unique characters ───────────────────────────────

    @Test
    @DisplayName("Edge 3 — all unique characters, entire string is the answer")
    void testAllUniqueChars() {
        assertEquals(6, solution.lengthOfLongestSubstring("abcdef"));
    }

    // ─── EDGE CASE 4: all same characters ─────────────────────────────────

    @Test
    @DisplayName("Edge 4 — all same character, answer is always 1")
    void testAllSameChar() {
        assertEquals(1, solution.lengthOfLongestSubstring("aaaaaaa"));
    }

    // ─── EDGE CASE 5: two characters alternating ──────────────────────────

    @Test
    @DisplayName("Edge 5 — two alternating chars, answer is 2")
    void testTwoAlternatingChars() {
        assertEquals(2, solution.lengthOfLongestSubstring("ababab"));
        // "ab" = 2
    }

    // ─── EDGE CASE 6: duplicate at start ──────────────────────────────────

    @Test
    @DisplayName("Edge 6 — duplicate appears at very start")
    void testDuplicateAtStart() {
        assertEquals(5, solution.lengthOfLongestSubstring("aabcde"));
        // "abcde" = 5, window must shrink past first 'a' correctly
    }

    // ─── EDGE CASE 7: duplicate at end ────────────────────────────────────

    @Test
    @DisplayName("Edge 7 — duplicate appears at very end")
    void testDuplicateAtEnd() {
        assertEquals(5, solution.lengthOfLongestSubstring("abcdea"));
        // "abcde" = 5, last 'a' triggers shrink but answer already recorded
    }

    // ─── EDGE CASE 8: longest window at start ─────────────────────────────

    @Test
    @DisplayName("Edge 8 — longest unique substring at the beginning")
    void testLongestAtStart() {
        assertEquals(4, solution.lengthOfLongestSubstring("abcdaa"));
        // "abcd" = 4
    }

    // ─── EDGE CASE 9: longest window at end ───────────────────────────────

    @Test
    @DisplayName("Edge 9 — longest unique substring at the end")
    void testLongestAtEnd() {
        assertEquals(5, solution.lengthOfLongestSubstring("aabcde"));
    }

    @Test
    @DisplayName("Edge 9b — longest at end corrected")
    void testLongestAtEndCorrected() {
        assertEquals(5, solution.lengthOfLongestSubstring("zzabcd"));
    }

    // ─── EDGE CASE 10: two character string ───────────────────────────────

    @Test
    @DisplayName("Edge 10a — two same chars returns 1")
    void testTwoSameChars() {
        assertEquals(1, solution.lengthOfLongestSubstring("aa"));
    }

    @Test
    @DisplayName("Edge 10b — two different chars returns 2")
    void testTwoDifferentChars() {
        assertEquals(2, solution.lengthOfLongestSubstring("ab"));
    }

    // ─── EDGE CASE 11: spaces and special characters ──────────────────────

    @Test
    @DisplayName("Edge 11a — string with spaces")
    void testStringWithSpaces() {
        assertEquals(3, solution.lengthOfLongestSubstring("a b c"));
        // "a b" or "b c" = 3 (a, space, b)
    }

    @Test
    @DisplayName("Edge 11b — string with special characters")
    void testSpecialCharacters() {
        assertEquals(5, solution.lengthOfLongestSubstring("a!b@c"));
        // all unique → 5
    }

    @Test
    @DisplayName("Edge 11c — string with digits")
    void testStringWithDigits() {
        assertEquals(4, solution.lengthOfLongestSubstring("abc1abc1"));
        // "abc1" = 4
    }

    // ─── EDGE CASE 12: non-ASCII / unicode characters ─────────────────────

    @Test
    @DisplayName("Edge 12 — unicode characters treated as distinct")
    void testUnicodeCharacters() {
        assertEquals(4, solution.lengthOfLongestSubstring("你好世界你"));
        // "你好世" = 3 unique, then '界' makes 4, then '你' repeats → 4
    }

    @Test
    @DisplayName("Edge 12b — unicode corrected")
    void testUnicodeCorrected() {
        assertEquals(4, solution.lengthOfLongestSubstring("你好世界你"));
        // "你好世界" = 4, then '你' repeats
    }

    // ─── EDGE CASE 13: repeat appears far apart ───────────────────────────

    @Test
    @DisplayName("Edge 13 — same char repeats far apart, window slides correctly")
    void testRepeatFarApart() {
        assertEquals(7, solution.lengthOfLongestSubstring("abcdefga"));
        // "abcdefg" = 7, then 'a' repeats at index 7
    }

    // ─── EDGE CASE 14: multiple repeats close together ────────────────────

    @Test
    @DisplayName("Edge 14 — multiple different repeats clustered together")
    void testMultipleRepeatsClose() {
        assertEquals(4, solution.lengthOfLongestSubstring("abcbad"));
        // a(0)b(1)c(2)b(3) → 'b' repeats, window shrinks to c(2)b(3) = 2
        // then a(4): "cba" = 3, then d(5): "cbad" = 4
        // Actually: "cbad" = 4
    }

    @Test
    @DisplayName("Edge 14b — corrected multiple repeats")
    void testMultipleRepeatsCorrected() {
        assertEquals(4, solution.lengthOfLongestSubstring("abcbad"));
        // "cbad" = 4
    }

    // ─── EDGE CASE 15: large input ────────────────────────────────────────

    @Test
    @DisplayName("Edge 15 — large string all unique, O(n) required")
    void testLargeAllUnique() {
        // Build a string of 26 unique chars repeated 1000 times
        // Each window of 26 chars is the max
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            for (char c = 'a'; c <= 'z'; c++) {
                sb.append(c);
            }
        }
        assertEquals(26, solution.lengthOfLongestSubstring(sb.toString()));
    }

    @Test
    @DisplayName("Edge 15b — large string all same char")
    void testLargeAllSameChar() {
        String input = "a".repeat(100000);
        assertEquals(1, solution.lengthOfLongestSubstring(input));
    }
}