package GoldmanSach.StringCompression;

public class StringCompression {

    public static void main(String[] args) {
        StringCompression stringCompress = new StringCompression();

        char[] testChar = { 'a', 'a', 'b', 'b', 'c', 'c', 'c' };
        int index = stringCompress.compress(testChar);
        System.out.println("total count: " + index);
        System.out.println(stringCompress.compress(null));
        System.out.println(stringCompress.compress(new char[] {}));
        System.out.println(stringCompress.compress(new char[] { 'a' }));
        System.out.println(stringCompress.compress(new char[] { 'a', 'a', 'a', 'a', 'a', 'a', 'a' }));
    }


    /**
     * Compresses an array of characters in-place using the Read-Write pointer technique.
     * Time Complexity: O(N) - Each character is visited once by the 'read' pointer.
     * Space Complexity: O(1) - No extra data structures used regardless of input size.
     */
    public int compress(char[] chars) {
        // 1. Edge Case: An empty array has a compressed length of 0
        if (chars == null || chars.length == 0){
            return 0;
        }

        int read = 0;  // The "Explorer": Scans through the original array
        int write = 0; // The "Editor": Keeps track of where to overwrite the result

        while (read < chars.length) {
            char currentChar = chars[read];
            int count = 0;

            // 2. Grouping Phase:
            // Move the 'read' pointer forward to find the end of the current repeating block
            while (read < chars.length && chars[read] == currentChar){
                read++;
                count++;
            }

            // 3. Writing Phase (Character):
            // Always write the character itself at the current 'write' position
            chars[write++] = currentChar;

            // 4. Writing Phase (Count):
            // If the character appeared more than once, convert the count to a string
            // and write each digit into the array.
            // Example: count 12 becomes '1' then '2'
            if (count > 1) {
                for (char c: Integer.toString(count).toCharArray()){
                    chars[write++] = c;
                }
            }
        }

        // 5. Result:
        // The 'write' pointer represents the length of the new compressed portion
        return write;
    }

}
