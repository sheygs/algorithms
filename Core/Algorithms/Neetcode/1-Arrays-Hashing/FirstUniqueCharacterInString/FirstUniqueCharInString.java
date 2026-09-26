package GoldmanSachs.FirstUniqueCharInString;

import java.util.HashMap;
import java.util.Map;

public class FirstUniqueCharInString {
      /*
         store in a hash map
         {
         "a": 0,
         "c": 1,
         ...
         }
      */
   public static void main(String[] args) {
      FirstUniqueCharInString t = new FirstUniqueCharInString();
      int result = t.firstUniqChar("ssssaaaaabbc");
      System.out.printf("result: %d\n", result);
   }


   /**
    * Finds the index of the first non-repeating character in a string.
    * Time Complexity: O(N), where N is the length of the string (two linear passes).
    * Space Complexity: O(1) technically, as the map size is capped by the alphabet (e.g., 26 for English).
    */
   public int firstUniqChar(String s) {
      // 1. Validation: Handle empty or null input strings
      if (s == null || s.isEmpty()) {
         return -1;
      }

      // 2. Frequency Mapping:
      // We use a HashMap to store the character as the key and its occurrences as the value.
      Map<Character, Integer> countMap = new HashMap<>();

      // First pass: Build the character frequency table
      for (char c : s.toCharArray()) {
         countMap.put(c, countMap.getOrDefault(c, 0) + 1);
      }

      // 3. Identification:
      // Second pass: Iterate through the string *in order* to find
      // the first character that has a frequency count of exactly 1.
      for (int i = 0; i < s.length(); i++) {
         if (countMap.get(s.charAt(i)) == 1) {
               return i; // Return immediately upon finding the first unique char
         }
      }

      // 4. Default: Return -1 if no unique character exists
      return -1;
   }
}


