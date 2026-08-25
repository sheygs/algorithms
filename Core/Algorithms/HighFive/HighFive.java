package GoldmanSachs.HighFive;

import java.util.*;
public class HighFive {

    public int[][] highFiveBrute(int[][] items) {
        /* * BRUTE FORCE ALTERNATIVE:
            * 1. Map<Integer, List<Integer>> to store ALL scores: O(N) space.
            * 2. Sort each list: O(N log N) time.
            * 3. Sort the keys (Student IDs): O(S log S) time.
        */

        // check if null or empty array
        if (items == null || items.length == 0) {
            return new int[0][0];
        }

        // sort array - O(nlogn)
        // Are the two items (i.e ID) from different student?
        // if YES, sort by their ID in ASC order
        // if NO, (i.e. same student, sort by their scores in DESC order)
        // so first 5 scores per student are automatically the top 5
        Arrays.sort(items, (a, b) -> a[0] != b[0]
                ? Integer.compare(a[0], b[0])   // sort by ID ascending
                : Integer.compare(b[1], a[1])); // sort by score descending


        List<int[]> result = new ArrayList<>();
        int i = 0;

        while (i < items.length) {
            int studentId = items[i][0];
            int sum = 0;
            int count = 0;

            // collect top 5 scores for this student
            // while (i < items.length && items[i][0] == studentId && count < 5) {
            //     sum += items[i][1];
            //     count++;
            //     i++;
            // }

            // skip remaining scores for this student (already have top 5)
            // while (i < items.length && items[i][0] == studentId) {
            //     i++;
            // }

            // collect top 5 scores for this student
            while (i < items.length && items[i][0] == studentId) {
                if (count < 5) {
                    sum += items[i][1];
                    count++;
                }
                // skip remaining scores for this student (already have top 5)
                i++;
            }

            result.add(new int[] { studentId, sum / count });
        }

        return result.toArray(new int[result.size()][]);
    }

    /**
     * Computes the average of the top five scores for each student.
     * Time Complexity: O(N log K), where N is the number of items and K is the heap size (5).
     * Space Complexity: O(S), where S is the number of unique students.
     */
    public int[][] highFive(int[][] items) {
        // 1. Validation: Handle edge cases for empty input
        if (items == null || items.length == 0) {
            return new int[0][0];
        }

        /* * 2. Data Structure Setup:
            * We use a TreeMap to store Student IDs as keys and a Min-Heap (PriorityQueue) as values.
            * TreeMap: Automatically keeps Student IDs in ascending order, satisfying the output requirement.
            * PriorityQueue: Acts as a "sliding window" for the top 5 scores.
        */
        Map<Integer, PriorityQueue<Integer>> studentScoresMap = new TreeMap<>();

        for (int[] item : items) {
            int studentId = item[0];
            int score = item[1];

            // Ensure a PriorityQueue exists for the student before adding scores
            studentScoresMap.computeIfAbsent(studentId, k -> new PriorityQueue<>());

            PriorityQueue<Integer> minHeap = studentScoresMap.get(studentId);
            minHeap.offer(score);

            /*
            * 3. Maintaining the Top 5:
            * Since this is a Min-Heap, the smallest score is always at the root (top).
            * If the size exceeds 5, we remove (poll) the smallest score.
            * This ensures only the 5 largest scores encountered so far remain in the heap.
            */
            if (minHeap.size() > 5) {
                minHeap.poll();
            }
        }

        // 4. Result Construction: Initialize result array based on the number of unique students
        int[][] result = new int[studentScoresMap.size()][2];
        int index = 0;

        /*
        * 5. Final Calculation:
        * Iterate through the TreeMap. Since it's a TreeMap, we process students in
        * ascending ID order automatically.
        */
        for (var entry : studentScoresMap.entrySet()) {
            int studentId = entry.getKey();
            PriorityQueue<Integer> topFiveHeap = entry.getValue();

            int sum = 0;
            // The heap contains exactly the top 5 scores (or fewer if the student has less than 5)
            while (!topFiveHeap.isEmpty()) {
                sum += topFiveHeap.poll();
            }

            // Calculate integer average and store in the result
            result[index][0] = studentId;
            result[index][1] = sum / 5;
            index++;
        }

        return result;
    }

    // High Five variant 1
    // Computes average scores per student and not top5
    // does not require min-heap
    public int[][] averageScore(int[][] items) {
        if (items == null || items.length == 0) {
            return new int[0][0];
        }

        // int[0] = sum, int[1] = count
        Map<Integer, int[]> studentScoresMap = new TreeMap<>();

        for (int[] item : items) {
            int studentId = item[0];
            int score = item[1];

            studentScoresMap.computeIfAbsent(studentId, k -> new int[2]);

            studentScoresMap.get(studentId)[0] += score; // accumulate sum
            studentScoresMap.get(studentId)[1]++; // increment count

            studentScoresMap.forEach((id, stats) -> {
                System.out.println("studentID: " + id + ", sum: " + stats[0] + ", count: " + stats[1]);
            });
        }

        System.out.println("studentScoresMap: " + Arrays.deepToString(studentScoresMap.entrySet().toArray()));

        int[][] result = new int[studentScoresMap.size()][2];
        int index = 0;

        for (var entry : studentScoresMap.entrySet()) {
            int studentId = entry.getKey();
            // int sum = entry.getValue()[0];
            // int count = entry.getValue()[1];

            // result[index][0] = studentId;
            // result[index][1] = sum / count; // integer division = floor average
            // index++;
            int[] stats = entry.getValue();
            int average = stats[0] / stats[1];

            result[index++] = new int[] { studentId, average }; // integer division = floor average
        }

        return result;
    }

    // High Five variant 2
    // Top-N average - gets average of top-N scores per student
    // More flexible than HighFive, which is top-5 scores
    public int[][] highNAverage(int[][] items, int n) {
        // 1. Validation: Handle edge cases for empty input
        if (items == null || items.length == 0) {
            return new int[0][0];
        }

        /* * 2. Data Structure Setup:
            * We use a TreeMap to store Student IDs as keys and a Min-Heap (PriorityQueue) as values.
            * TreeMap: Automatically keeps Student IDs in ascending order, satisfying the output requirement.
            * PriorityQueue: Acts as a "sliding window" for the top N scores.
        */
        Map<Integer, PriorityQueue<Integer>> studentScoresMap = new TreeMap<>();

        for (int[] item : items) {
            int studentId = item[0];
            int score = item[1];

            // Ensure a PriorityQueue exists for the student before adding scores
            studentScoresMap.computeIfAbsent(studentId, k -> new PriorityQueue<>());

            PriorityQueue<Integer> minHeap = studentScoresMap.get(studentId);
            minHeap.offer(score);

            // 3. Maintaining the Top N:
            // Since this is a Min-Heap, the smallest score is always at the root (top).
            // If the size exceeds N, we remove (poll) the smallest score.
            // This ensures only the N largest scores encountered so far remain in the heap

            if (minHeap.size() > n) {
                minHeap.poll();
            }
        }

        // 4. Result Construction: Initialize result array based on the number of unique students
        int[][] result = new int[studentScoresMap.size()][2];
        int index = 0;

        /*
        * 5. Final Calculation:
        * Iterate through the TreeMap. Since it's a TreeMap, we process students in
        * ascending ID order automatically.
        */
        for (var entry : studentScoresMap.entrySet()) {
            int studentId = entry.getKey();
            PriorityQueue<Integer> topNHeap = entry.getValue();


            int actualCount = topNHeap.size(); // ← capture BEFORE draining
            int sum = 0;
            // The heap contains exactly the top 5 scores (or fewer if the student has less than 5)
            while (!topNHeap.isEmpty()) {
                sum += topNHeap.poll();
            }

            // Calculate integer average and store in the result
            result[index][0] = studentId;
            result[index][1] = sum / Math.min(n, actualCount);
            index++;
        }

        return result;
    }

    // High Five variant 3
    // Returns highest single score per student
    // No heap, no sum/count — just max tracking
    public int[][] highestScore(int[][] items) {
        if (items == null || items.length == 0) {
            return new int[0][0];
        }

        // int value = max score seen so far for this student
        Map<Integer, Integer> studentScoresMap = new TreeMap<>();

        for (int[] item : items) {
            int studentId = item[0];
            int score = item[1];

            // If student not seen → initialise with this score
            // If student seen → keep the higher of existing max and new score
            studentScoresMap.merge(studentId, score, Math::max);
        }

        int[][] result = new int[studentScoresMap.size()][2];
        int index = 0;

        for (var entry : studentScoresMap.entrySet()) {
            int studentId = entry.getKey();
            int maxStudentScore = entry.getValue();
            result[index++] = new int[] { studentId, maxStudentScore };
        }

        return result;
    }

    // High Five variant 4
    // Returns students whose average score is above the threshold
    // Adapts averageScore — adds threshold filter during result construction
    public int[][] aboveAverageThreshold(int[][] items, int threshold) {
        if (items == null || items.length == 0) {
            return new int[0][0];
        }

        // int[0] = sum, int[1] = count — identical to averageScore
        Map<Integer, int[]> studentScoresMap = new TreeMap<>();

        for (int[] item : items) {
            int studentId = item[0];
            int score = item[1];

            studentScoresMap.computeIfAbsent(studentId, k -> new int[2]);

            studentScoresMap.get(studentId)[0] += score;
            studentScoresMap.get(studentId)[1]++;
        }

        // Collect qualifying students first — size unknown until we filter
        List<int[]> qualifying = new ArrayList<>();

        for (var entry : studentScoresMap.entrySet()) {
            int[] stats = entry.getValue();
            int average = stats[0] / stats[1];

            if (average > threshold) { // ← only change
                qualifying.add(new int[] { entry.getKey(), average });
            }
        }

        return qualifying.toArray(new int[qualifying.size()][]);
    }

    public static void main(String[] args) {
        HighFive solution = new HighFive();

        // int[][] output = solution.highFive(new int[][]{
        //     {1,90},{1,85},{1,80},{1,75},{1,70},{1, 65},
        //     {2,90},{2,85},{2,80},{2,75},{2,70},
        //     {3,90},{3,85},{3,80},{3,75},{3,70}
        // });

        // System.out.println("solution 1: " + Arrays.deepToString(output));

        int [][] output2 = solution.averageScore(new int[][]{
            {1,90},{1,85},{1,80},{1,75},{1,70},{1, 65},
            {2,90},{2,85},{2,80},{2,75},{2,70},
            {3,90},{3,85},{3,80},{3,75},{3,70}
        });

        System.out.println("solution 2: " + Arrays.deepToString(output2));
    }
}
