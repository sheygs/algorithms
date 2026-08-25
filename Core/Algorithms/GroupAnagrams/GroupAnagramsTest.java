package GoldmanSachs.GroupAnagrams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GroupAnagramsTest {

    private GroupAnagrams solution;

    @BeforeEach
    void setUp() {
        solution = new GroupAnagrams();
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────
    // Order of groups and order within groups is not guaranteed — normalise both
    private List<List<String>> normalise(List<List<String>> result) {
        result.forEach(group -> java.util.Collections.sort(group));
        result.sort((a, b) -> a.get(0).compareTo(b.get(0)));
        return result;
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — three groups from mixed input")
    void testLeetCodeExample1() {
        String[] input = {"eat","tea","tan","ate","nat","bat"};
        List<List<String>> result = normalise(solution.groupAnagrams(input));
        List<List<String>> expected = normalise(Arrays.asList(
            Arrays.asList("ate","eat","tea"),
            Arrays.asList("nat","tan"),
            Arrays.asList("bat")
        ));
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("LC Example 2 — single empty string")
    void testLeetCodeExample2() {
        String[] input = {""};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("", result.get(0).get(0));
    }

    @Test
    @DisplayName("LC Example 3 — single character string")
    void testLeetCodeExample3() {
        String[] input = {"a"};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("a", result.get(0).get(0));
    }

    // ─── EDGE CASE 1: null / empty input ──────────────────────────────────

    @Test
    @DisplayName("Edge 1a — null input returns empty list")
    void testNullInput() {
        List<List<String>> result = solution.groupAnagrams(null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Edge 1b — empty array returns empty list")
    void testEmptyArray() {
        List<List<String>> result = solution.groupAnagrams(new String[]{});
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ─── EDGE CASE 2: all strings are anagrams of each other ──────────────

    @Test
    @DisplayName("Edge 2 — all words are anagrams, one group only")
    void testAllAnagrams() {
        String[] input = {"abc","bca","cab","bac","acb","cba"};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(1, result.size());
        assertEquals(6, result.get(0).size());
    }

    // ─── EDGE CASE 3: no anagrams at all ──────────────────────────────────

    @Test
    @DisplayName("Edge 3 — no two words are anagrams, each in its own group")
    void testNoAnagrams() {
        String[] input = {"abc","def","ghi","jkl"};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(4, result.size());
        result.forEach(group -> assertEquals(1, group.size()));
    }

    // ─── EDGE CASE 4: all empty strings ───────────────────────────────────

    @Test
    @DisplayName("Edge 4 — all empty strings are anagrams of each other")
    void testAllEmptyStrings() {
        String[] input = {"","",""};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).size());
    }

    // ─── EDGE CASE 5: single word ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 5 — single word always forms one group")
    void testSingleWord() {
        String[] input = {"hello"};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals("hello", result.get(0).get(0));
    }

    // ─── EDGE CASE 6: duplicate words ─────────────────────────────────────

    @Test
    @DisplayName("Edge 6 — duplicate words are anagrams of each other")
    void testDuplicateWords() {
        String[] input = {"abc","abc","abc"};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).size());
    }

    // ─── EDGE CASE 7: mixed lengths ───────────────────────────────────────

    @Test
    @DisplayName("Edge 7 — words of different lengths cannot be anagrams")
    void testMixedLengths() {
        String[] input = {"a","aa","aaa","b","bb"};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(5, result.size());
        result.forEach(group -> assertEquals(1, group.size()));
    }

    // ─── EDGE CASE 8: anagram groups of varied sizes ──────────────────────

    @Test
    @DisplayName("Edge 8 — groups of different sizes all correctly formed")
    void testVariedGroupSizes() {
        String[] input = {"eat","tea","ate","bat","tan","nat","arc","car","rac"};
        List<List<String>> result = normalise(solution.groupAnagrams(input));
        List<List<String>> expected = normalise(Arrays.asList(
            Arrays.asList("ate","eat","tea"),
            Arrays.asList("bat"),
            Arrays.asList("nat","tan"),
            Arrays.asList("arc","car","rac")
        ));
        assertEquals(expected, result);
    }

    // ─── EDGE CASE 9: single character strings ────────────────────────────

    @Test
    @DisplayName("Edge 9 — single char strings grouped correctly")
    void testSingleCharStrings() {
        String[] input = {"a","b","a","c","b","a"};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(3, result.size());
        // a:3, b:2, c:1
        result.forEach(group -> {
            String first = group.get(0);
            if (first.equals("a")) assertEquals(3, group.size());
            else if (first.equals("b")) assertEquals(2, group.size());
            else if (first.equals("c")) assertEquals(1, group.size());
        });
    }

    // ─── EDGE CASE 10: large input ────────────────────────────────────────

    @Test
    @DisplayName("Edge 10 — large input with two anagram groups")
    void testLargeInput() {
        int n = 10000;
        String[] input = new String[n * 2];
        for (int i = 0; i < n; i++) {
            input[i] = "abc";
            input[i + n] = "cab";
        }
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(1, result.size());
        assertEquals(n * 2, result.get(0).size());
    }

    // ─── EDGE CASE 11: words with repeated characters ─────────────────────

    @Test
    @DisplayName("Edge 11 — repeated chars in word, anagram grouping still correct")
    void testRepeatedCharsInWord() {
        String[] input = {"aab","baa","aba","bbb"};
        List<List<String>> result = normalise(solution.groupAnagrams(input));
        List<List<String>> expected = normalise(Arrays.asList(
            Arrays.asList("aab","aba","baa"),
            Arrays.asList("bbb")
        ));
        assertEquals(expected, result);
    }

    // ─── EDGE CASE 12: result size equals number of unique anagram groups ──

    @Test
    @DisplayName("Edge 12 — result list length equals number of unique groups")
    void testResultSizeEqualsUniqueGroups() {
        String[] input = {"eat","tea","tan","ate","nat","bat"};
        List<List<String>> result = solution.groupAnagrams(input);
        assertEquals(3, result.size(), "Should have exactly 3 anagram groups");
    }
}

