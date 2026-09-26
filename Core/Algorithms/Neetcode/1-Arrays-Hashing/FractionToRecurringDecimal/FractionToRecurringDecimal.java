package GoldmanSachs.FractionToRecurringDecimal;

import java.util.HashMap;
import java.util.Map;


public class FractionToRecurringDecimal {

    public static void main(String[] args) {
        FractionToRecurringDecimal fracToDec = new FractionToRecurringDecimal();
        System.out.println(fracToDec.fractionToDecimal(-1, 3));   // -0.(3)
        System.out.println(fracToDec.fractionToDecimal(1, 3));    // 0.(3)
        System.out.println(fracToDec.fractionToDecimal(-1, -2));  // 0.5
        System.out.println(fracToDec.fractionToDecimal(1, 4));    // 0.25
        System.out.println(fracToDec.fractionToDecimal(0, -2)); // 0
        System.out.println(fracToDec.fractionToDecimal(Integer.MIN_VALUE, 1));
    }

    /**
     * Converts a fraction to its decimal representation, detecting repeating patterns.
     * Time Complexity: O(Denominator) - In the worst case, we can only have as many unique
     * remainders as the value of the denominator before a cycle occurs.
     * Space Complexity: O(Denominator) - To store the position of each remainder in our map.
     */
    public String fractionToDecimal(int numerator, int denominator) {
        // 1. Validation & Edge Cases
        if (denominator == 0) throw new ArithmeticException("cannot divide by zero");
        if (numerator == 0) return "0";

        StringBuilder result = new StringBuilder();

        // 2. Sign Handling:
        // Use XOR (^) to determine if the result is negative.
        // This handles the case where one is negative and the other is positive.
        if ((numerator < 0) ^ (denominator < 0)) {
            result.append("-");
        }

        // Convert to long to prevent overflow (e.g., Integer.MIN_VALUE / -1)
        long num = Math.abs((long) numerator);
        long denom = Math.abs((long) denominator);

        // 3. Integer Part:
        // Append the whole number part and calculate the initial remainder.
        result.append(num / denom);
        long remainder = num % denom;

        // If there is no remainder, the division is exact.
        if (remainder == 0) return result.toString();

        // 4. Fractional Part:
        result.append(".");

        /* * Map tracks <Remainder, Position in StringBuilder>.
        * If we see the same remainder again, it means the sequence of digits
        * from that previous position will now repeat infinitely.
        */
        Map<Long, Integer> remainderMap = new HashMap<>();

        while (remainder != 0) {
            // Cycle Detection:
            if (remainderMap.containsKey(remainder)) {
                // We retrieve the index where we first saw this remainder.
                // This is exactly where the repeating digits started being appended.
                int cycleStart = remainderMap.get(remainder);
                result.insert(cycleStart, "("); // Wrap the start of the repeating part
                result.append(")");             // End the repeating part
                return result.toString();
            }

            // 3. THE "SNAPSHOT" (Recording):
            // Before we calculate the next digit, we record the current remainder
            // and the current length of our StringBuilder.
            // This length acts as a 'bookmark' for where the digit resulting
            // from this specific remainder will be placed.

            // Store current remainder and the index where its resulting digit will go
            remainderMap.put(remainder, result.length());

            // Standard Long Division:
            // Multiply remainder by 10 to "bring down a zero"
            remainder *= 10;
            result.append(remainder / denom);
            remainder %= denom;
        }

        return result.toString();
    }
}