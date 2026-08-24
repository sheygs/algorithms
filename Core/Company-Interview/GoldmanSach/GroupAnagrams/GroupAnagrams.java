package GoldmanSachs.GroupAnagrams;

import java.util.*;

    /*
        HashMap
        {
         "[1, 0, 0, 0, 1,...]": ["eat", "tea", "ate"]
        }

        returns: [
                ["eat", "tea","ate"],
                ["tan", "nat"],
                ["bat"]
        ]
    */


public class GroupAnagrams {

    public static void main(String[] args) {
        GroupAnagrams groupAnagInstance = new GroupAnagrams();
        List<List<String>> result = groupAnagInstance
                .groupAnagrams(new String[] { "eat", "tea", "tan", "ate", "nat", "bat" });
        System.out.println(result);
    }

    /**
     * Groups strings that are anagrams of each other.
     * Time Complexity: O(N * K), where N is the number of strings and K is the max length of a string.
     * Space Complexity: O(N * K), to store the grouped strings in the HashMap.
     */
    public List<List<String>> groupAnagrams(String[] strs) {
        // 1. Validation: Handle empty input
        if (strs == null || strs.length == 0) {
            return new ArrayList<>();
        }

        /* * 2. The Strategy:
        * Two words are anagrams if they have the exact same character counts.
        * We use a HashMap where:
        * Key: A unique string representation of the character frequency (e.g., "[1, 0, 2...]")
        * Value: A list of words that match that frequency profile.
        */
        Map<String, List<String>> anagramGroups = new HashMap<>();

        for (String word : strs) {
            // 3. Frequency Counting:
            // Create a count of each letter 'a'-'z'.
            // This is more efficient than sorting for long strings.
            int[] charCount = new int[26];
            for (char c : word.toCharArray()) {
                charCount[c - 'a']++;
            }

            /* * 4. Key Generation:
            * Transform the array into a String to use as a Map key.
            * Arrays.toString() creates a unique "fingerprint" for each anagram group.
            * Example: "eat" and "tea" both become "[1, 0, 0, 0, 1, 0, ..., 1, 0...]"
            */
            String key = Arrays.toString(charCount);

            // Add the word to the appropriate list in the map
            anagramGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
        }

        // 5. Final Output: Return only the grouped lists from the map values
        return new ArrayList<>(anagramGroups.values());
    }
}