package GoldmanSachs.DecodeString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class DecodeStringTest {

   private DecodeString solution;

   @BeforeEach
   void setUp() {
      solution = new DecodeString();
   }

   // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

   @Test
   @DisplayName("LC Example 1 — single group 3[a]")
   void testLeetCodeExample1() {
      assertEquals("aaabcbc", solution.decodeString("3[a]2[bc]"));
      // 3[a] = "aaa", 2[bc] = "bcbc" → "aaabcbc"
   }

   @Test
   @DisplayName("LC Example 2 — nested groups 3[a2[c]]")
   void testLeetCodeExample2() {
      assertEquals("accaccacc", solution.decodeString("3[a2[c]]"));
      // inner: 2[c]="cc", outer: 3[a+"cc"]="accaccacc"
   }

   @Test
   @DisplayName("LC Example 3 — consecutive and nested groups")
   void testLeetCodeExample3() {
      assertEquals("abcabccdcdcdef", solution.decodeString("2[abc]3[cd]ef"));
      // 2[abc]="abcabc", 3[cd]="cdcdcd", ef="ef"
   }

   @Test
   @DisplayName("LC Example 4 — 10[a] followed by b")
   void testLeetCodeExample4() {
      assertEquals("aaaaaaaaaab", solution.decodeString("10[a]b"));
      //             ^^^^^^^^^^  — 10 a's then b
   }

   // ─── EDGE CASE 1: null / empty ─────────────────────────────────────────

   @Test
   @DisplayName("Edge 1a — null input returns empty string")
   void testNullInput() {
      assertEquals("", solution.decodeString(null));
   }

   @Test
   @DisplayName("Edge 1b — empty string returns empty string")
   void testEmptyString() {
      assertEquals("", solution.decodeString(""));
   }

   // ─── EDGE CASE 2: no encoding — plain string ───────────────────────────

   @Test
   @DisplayName("Edge 2 — no brackets, string returned as-is")
   void testPlainString() {
      assertEquals("abc", solution.decodeString("abc"));
   }

   // ─── EDGE CASE 3: single character repeated ────────────────────────────

   @Test
   @DisplayName("Edge 3 — single char repeated once")
   void testSingleCharRepeatOnce() {
      assertEquals("a", solution.decodeString("1[a]"));
   }

   @Test
   @DisplayName("Edge 3b — single char repeated many times")
   void testSingleCharRepeatMany() {
      assertEquals("aaaaaaaaaa", solution.decodeString("10[a]"));
   }

   // ─── EDGE CASE 4: repeat count = 0 ────────────────────────────────────

   @Test
   @DisplayName("Edge 4 — repeat count 0 produces empty string for that group")
   void testRepeatCountZero() {
      assertEquals("bc", solution.decodeString("0[a]bc"));
      // 0[a] = "", bc = "bc"
   }

   // ─── EDGE CASE 5: nested brackets ─────────────────────────────────────

   @Test
   @DisplayName("Edge 5a — two levels of nesting")
   void testTwoLevelsNesting() {
      assertEquals("abbabbabb", solution.decodeString("3[a2[b]]"));
      // inner: 2[b] = "bb"
      // outer: 3[a + "bb"] = 3["abb"] = "abbabbabb"
   }

   @Test
   @DisplayName("Edge 5b — two levels of nesting corrected")
   void testTwoLevelsNestingCorrected() {
      assertEquals("abbabbabb", solution.decodeString("3[a2[b]]"));
      // inner: 2[b]="bb", outer: 3[a+bb]="abbabbabb"
   }


   @Test
   @DisplayName("Edge 5c — three levels of nesting")
   void testThreeLevelsNesting() {
      assertEquals("aabaab", solution.decodeString("2[2[a]b]"));
      // inner: 2[a] = "aa"
      // outer: 2["aa" + "b"] = 2["aab"] = "aabaab"
   }

   @Test
   @DisplayName("Edge 5d — three levels corrected")
   void testThreeLevelsCorrected() {
      assertEquals("aabaab", solution.decodeString("2[2[a]b]"));
      // 2[a]="aa", "aa"+"b"="aab", 2["aab"]="aabaab"
   }

   // ─── EDGE CASE 6: letters before and after brackets ───────────────────

   @Test
   @DisplayName("Edge 6a — letters before bracket group")
   void testLettersBeforeBracket() {
      assertEquals("xyzaaa", solution.decodeString("xyz3[a]"));
   }

   @Test
   @DisplayName("Edge 6b — letters after bracket group")
   void testLettersAfterBracket() {
      assertEquals("aaaxyz", solution.decodeString("3[a]xyz"));
   }

   @Test
   @DisplayName("Edge 6c — letters both before and after bracket group")
   void testLettersBothSides() {
      assertEquals("xaaay", solution.decodeString("x3[a]y"));
   }

   // ─── EDGE CASE 7: multi-digit repeat count ────────────────────────────

   @Test
   @DisplayName("Edge 7 — repeat count has multiple digits")
   void testMultiDigitCount() {
      assertEquals("a".repeat(100), solution.decodeString("100[a]"));
   }

   // ─── EDGE CASE 8: consecutive bracket groups ──────────────────────────

   @Test
   @DisplayName("Edge 8 — multiple consecutive bracket groups")
   void testConsecutiveBracketGroups() {
      assertEquals("aabbcc", solution.decodeString("2[a]2[b]2[c]"));
   }

   // ─── EDGE CASE 9: nested with letters inside ──────────────────────────

   @Test
   @DisplayName("Edge 9 — letters mixed inside bracket group")
   void testLettersMixedInsideBracket() {
      assertEquals("acdacd", solution.decodeString("2[acd]"));
      // 2["acd"] = "acdacd"
   }

   @Test
   @DisplayName("Edge 9b — letters mixed inside nested corrected")
   void testLettersMixedInsideNestedCorrected() {
      assertEquals("acdacd", solution.decodeString("2[acd]"));
   }

   // ─── EDGE CASE 10: deeply nested ──────────────────────────────────────

   @Test
   @DisplayName("Edge 10 — four levels of nesting")
   void testFourLevelsNesting() {
      assertEquals("aaaa", solution.decodeString("2[2[a]]"));
      // inner: 2[a]="aa", outer: 2["aa"]="aaaa"
   }

   // ─── EDGE CASE 11: single bracket group ───────────────────────────────

   @Test
   @DisplayName("Edge 11 — entire string is one bracket group")
   void testEntireStringOneBracketGroup() {
      assertEquals("abcabcabc", solution.decodeString("3[abc]"));
   }

   // ─── EDGE CASE 12: large repeat with nested ───────────────────────────

   @Test
   @DisplayName("Edge 12 — large repeat count with nested groups")
   void testLargeRepeatNested() {
      String result = solution.decodeString("10[a2[b]]");
      assertEquals("abb".repeat(10), result);
      // inner: 2[b]="bb", outer: 10["a"+"bb"]="abb"*10
   }

   // ─── EDGE CASE 13: stack depth ────────────────────────────────────────

   @Test
   @DisplayName("Edge 13 — deeply nested brackets stress test")
   void testDeeplyNestedStress() {
      // 2[2[2[a]]] = 2[2["aa"]] = 2["aaaa"] = "aaaaaaaa"
      assertEquals("aaaaaaaa", solution.decodeString("2[2[2[a]]]"));
   }
}