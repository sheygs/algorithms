package GoldmanSachs.FractionToRecurringDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class FractionToRecurringDecimalTest {

    private FractionToRecurringDecimal solution;

    @BeforeEach
    void setUp() {
        solution = new FractionToRecurringDecimal();
    }

    // ─── LC EXAMPLES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — exact division, no decimal")
    void testLeetCodeExample1() {
        // Wait: 1/2 = 0.5 not 2. LC Example 1 is 1/2 = "0.5"
        assertEquals("0.5", solution.fractionToDecimal(1, 2));
    }

    @Test
    @DisplayName("LC Example 2 — exact integer result")
    void testLeetCodeExample2() {
        assertEquals("2", solution.fractionToDecimal(2, 1));
    }

    @Test
    @DisplayName("LC Example 3 — repeating decimal")
    void testLeetCodeExample3() {
        assertEquals("1.(3)", solution.fractionToDecimal(4, 3));
        // 4/3 = 1.333... wait: 4/3 = 1.(3)
        // LC Example 3 is actually 4/9 = 0.(4) or 1/6 = 0.1(6)
        // Let me use the actual LC examples:
    }

    // ─── ACTUAL LC EXAMPLES ───────────────────────────────────────────────────

    @Test
    @DisplayName("LC — 1/2 = terminating decimal")
    void testOneHalf() {
        assertEquals("0.5", solution.fractionToDecimal(1, 2));
    }

    @Test
    @DisplayName("LC — 2/1 = integer")
    void testTwoOverOne() {
        assertEquals("2", solution.fractionToDecimal(2, 1));
    }

    @Test
    @DisplayName("LC — 4/333 = repeating")
    void testFourOver333() {
        assertEquals("0.(012)", solution.fractionToDecimal(4, 333));
    }

    @Test
    @DisplayName("LC — 1/6 = non-repeating then repeating")
    void testOneSixth() {
        assertEquals("0.1(6)", solution.fractionToDecimal(1, 6));
        // 1/6 = 0.16666... → 0.1(6)
    }

    // ─── EDGE CASE 1: null / zero denominator ─────────────────────────────

    @Test
    @DisplayName("Edge 1 — denominator is zero, should throw or handle gracefully")
    void testZeroDenominator() {
        assertThrows(ArithmeticException.class,
                () -> solution.fractionToDecimal(1, 0));
    }

    // ─── EDGE CASE 2: numerator is zero ───────────────────────────────────

    @Test
    @DisplayName("Edge 2 — numerator is zero, result is always 0")
    void testNumeratorZero() {
        assertEquals("0", solution.fractionToDecimal(0, 5));
        assertEquals("0", solution.fractionToDecimal(0, -5));
        assertEquals("0", solution.fractionToDecimal(0, Integer.MAX_VALUE));
    }

    // ─── EDGE CASE 3: negative results ────────────────────────────────────

    @Test
    @DisplayName("Edge 3a — negative numerator")
    void testNegativeNumerator() {
        assertEquals("-0.5", solution.fractionToDecimal(-1, 2));
    }

    @Test
    @DisplayName("Edge 3b — negative denominator")
    void testNegativeDenominator() {
        assertEquals("-0.5", solution.fractionToDecimal(1, -2));
    }

    @Test
    @DisplayName("Edge 3c — both negative, result is positive")
    void testBothNegative() {
        assertEquals("0.5", solution.fractionToDecimal(-1, -2));
    }

    @Test
    @DisplayName("Edge 3d — negative repeating decimal")
    void testNegativeRepeating() {
        assertEquals("-0.(3)", solution.fractionToDecimal(-1, 3));
    }

    // ─── EDGE CASE 4: exact integer results ───────────────────────────────

    @Test
    @DisplayName("Edge 4a — result is exactly 1")
    void testResultIsOne() {
        assertEquals("1", solution.fractionToDecimal(3, 3));
    }

    @Test
    @DisplayName("Edge 4b — result is large integer")
    void testLargeInteger() {
        assertEquals("1000", solution.fractionToDecimal(1000, 1));
    }

    @Test
    @DisplayName("Edge 4c — negative integer result")
    void testNegativeInteger() {
        assertEquals("-2", solution.fractionToDecimal(-4, 2));
    }

    // ─── EDGE CASE 5: terminating decimals ────────────────────────────────

    @Test
    @DisplayName("Edge 5a — 1/4 terminates")
    void testOneQuarter() {
        assertEquals("0.25", solution.fractionToDecimal(1, 4));
    }

    @Test
    @DisplayName("Edge 5b — 1/8 terminates")
    void testOneEighth() {
        assertEquals("0.125", solution.fractionToDecimal(1, 8));
    }

    @Test
    @DisplayName("Edge 5c — 3/4 terminates")
    void testThreeQuarters() {
        assertEquals("0.75", solution.fractionToDecimal(3, 4));
    }

    // ─── EDGE CASE 6: repeating decimals ──────────────────────────────────

    @Test
    @DisplayName("Edge 6a — 1/3 repeats from first decimal place")
    void testOneThird() {
        assertEquals("0.(3)", solution.fractionToDecimal(1, 3));
    }

    @Test
    @DisplayName("Edge 6b — 2/3 repeats")
    void testTwoThirds() {
        assertEquals("0.(6)", solution.fractionToDecimal(2, 3));
    }

    @Test
    @DisplayName("Edge 6c — 1/7 long repeating cycle")
    void testOneSeventh() {
        assertEquals("0.(142857)", solution.fractionToDecimal(1, 7));
    }

    @Test
    @DisplayName("Edge 6d — 1/9 repeats")
    void testOneNinth() {
        assertEquals("0.(1)", solution.fractionToDecimal(1, 9));
    }

    @Test
    @DisplayName("Edge 6e — 1/99 repeats with leading zero in cycle")
    void testOneOver99() {
        assertEquals("0.(01)", solution.fractionToDecimal(1, 99));
    }

    // ─── EDGE CASE 7: non-repeating then repeating ────────────────────────

    @Test
    @DisplayName("Edge 7a — 1/6 has non-repeating part then repeating")
    void testMixedDecimal() {
        assertEquals("0.1(6)", solution.fractionToDecimal(1, 6));
    }

    @Test
    @DisplayName("Edge 7b — 1/12 non-repeating then repeating")
    void testOneTwelfth() {
        assertEquals("0.08(3)", solution.fractionToDecimal(1, 12));
    }

    // ─── EDGE CASE 8: Integer overflow ────────────────────────────────────

    @Test
    @DisplayName("Edge 8a — Integer.MIN_VALUE numerator causes overflow if cast naively")
    void testIntegerMinValueNumerator() {
        // -2147483648 / 1 = "-2147483648"
        // Must cast to long to avoid overflow
        assertEquals("-2147483648", solution.fractionToDecimal(Integer.MIN_VALUE, 1));
    }

    @Test
    @DisplayName("Edge 8b — Integer.MIN_VALUE denominator")
    void testIntegerMinValueDenominator() {
        // 1 / -2147483648
        assertEquals("-0.0000000004656612873077392578125",
                solution.fractionToDecimal(1, Integer.MIN_VALUE));
        // This terminates — MIN_VALUE = -2^31, only factors of 2
    }

    @Test
    @DisplayName("Edge 8c — both Integer.MAX_VALUE")
    void testBothMaxValue() {
        assertEquals("1", solution.fractionToDecimal(
                Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    // ─── EDGE CASE 9: result has no leading zero issues ───────────────────

    @Test
    @DisplayName("Edge 9 — result between -1 and 0 must have leading zero")
    void testLeadingZeroNegative() {
        assertEquals("-0.5", solution.fractionToDecimal(-1, 2));
        // Must be "-0.5" NOT "-.5"
    }

    // ─── EDGE CASE 10: large repeating cycle ──────────────────────────────

    @Test
    @DisplayName("Edge 10 — 1/17 has long repeating cycle of length 16")
    void testLongRepeatingCycle() {
        assertEquals("0.(0588235294117647)", solution.fractionToDecimal(1, 17));
    }
}