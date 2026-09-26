package GoldmanSachs.DecodeString;

import java.util.ArrayDeque;
import java.util.Deque;

// TC: O(n * maxK) — n = length of string, maxK = max repeat count
// SC: O(n)        — stack depth proportional to nesting levels
public class DecodeString {

    public static void main(String[] args) {
        DecodeString solution = new DecodeString();
        System.out.println(solution.decodeString("3[a]2[bc]"));    // aaabcbc
        System.out.println(solution.decodeString("3[a2[c]]"));     // accaccacc
        System.out.println(solution.decodeString("2[abc]3[cd]ef")); // abcabccdcdcdef
    }

    public String decodeString(String s) {
        if (s == null || s.isEmpty()) return "";

        Deque<Integer> countStack = new ArrayDeque<>();
        Deque<StringBuilder> stringStack = new ArrayDeque<>();
        StringBuilder current = new StringBuilder();
        int k = 0;

        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                // Build multi-digit number e.g. "12" → k=1 then k=12
                k = k * 10 + (c - '0');

            } else if (c == '[') {
                // Push current count and string onto stacks, reset both
                countStack.push(k);
                stringStack.push(current);
                current = new StringBuilder();
                k = 0;

            } else if (c == ']') {
                // Pop count and previous string
                int count = countStack.pop();
                StringBuilder prev = stringStack.pop();
                // Append current string repeated count times to prev
                for (int i = 0; i < count; i++) {
                    prev.append(current);
                }
                current = prev;

            } else {
                // Regular letter — append to current
                current.append(c);
            }
        }

        return current.toString();
    }
}