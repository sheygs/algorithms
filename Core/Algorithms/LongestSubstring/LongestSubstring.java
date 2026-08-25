package GoldmanSachs.LongestSubstring;

import java.util.HashSet;

/**
 * Brute force: for each char position, check every substring if it has any duplicate,
 * if it doesn't, return the length of the longest substring
 * O(n^2)
 */
public class LongestSubstring {
    /**
     * Sliding Window
     * Finds the length of the longest substring without repeating characters.
     * Time Complexity: O(N) - Each character is visited at most twice (once by 'right', once by 'left').
     * Space Complexity: O(min(N, M)) - N is string length, M is the size of the character set (alphabet).
     */
    public int lengthOfLongestSubstring(String s) {
        // 1. Validation: Handle null or empty strings early
        if (s == null || s.isEmpty()) {
            return 0;
        }

        // 2. Data Structure Setup:
        // We use a HashSet as a "sliding window" tracker to check for duplicates in O(1) time.
        HashSet<Character> visitedChars = new HashSet<>();
        int left = 0;    // The 'start' of our current unique substring
        int maxLength = 0;

        /*
        * 3. The Sliding Window Strategy:
        * The 'right' pointer expands the window by exploring new characters.
        */
        for (int right = 0; right < s.length(); right++) {
            char currentChar = s.charAt(right);

            /*
            * 4. Violation Handling:
            * If the character at 'right' is already in our set, the window is no longer unique.
            * We shrink the window from the 'left' until the duplicate is removed.
            */
            while (visitedChars.contains(currentChar)) {
                // Remove the character at the left pointer and move left inward
                visitedChars.remove(s.charAt(left));
                left++;
            }

            // 5. Update:
            // Add the new unique character and recalculate the maximum length found so far.
            visitedChars.add(currentChar);

            // (right - left + 1) gives us the current window size
            maxLength = Math.max(maxLength, right - left + 1);
        }

        return maxLength;
    }
}
