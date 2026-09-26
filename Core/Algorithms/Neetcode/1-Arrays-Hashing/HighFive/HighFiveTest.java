package GoldmanSachs.HighFive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class HighFiveTest {

    private HighFive solution;

    @BeforeEach
    void setUp() {
        solution = new HighFive();
    }

    // ─── EDGE CASE: null or empty input ───────────────────────────────────────

    @Test
    @DisplayName("Edge — null input returns empty 2D array")
    void testNullInput() {
        int[][] result = solution.highFive(null);
        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.length, "Result should be empty for null input");
    }

    @Test
    @DisplayName("Edge — empty input array returns empty 2D array")
    void testEmptyInput() {
        int[][] result = solution.highFive(new int[0][0]);
        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.length, "Result should be empty for empty input");
    }

    // ─── BASIC CASES ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("LC Example 1 — two students, mixed input order")
    void testLeetCodeExample1() {
        int[][] items = {
            {1,91},{1,92},{2,93},{2,97},{1,60},
            {2,77},{1,36},{1,90},{1,45},{1,94},
            {1,100},{2,100},{2,76}
        };
        int[][] expected = {{1,93},{2,88}};
        // Student 1 top 5: [100,94,92,91,90] = 467/5 = 93
        // Student 2 top 5: [100,97,93,77,76] = 443/5 = 88
        assertArrayEquals(expected, solution.highFive(items));
    }

    @Test
    @DisplayName("LC Example 2 — all top scores are 100")
    void testLeetCodeExample2() {
        int[][] items = {
            {1,100},{7,100},{1,100},{7,100},{1,100},
            {7,100},{1,100},{7,100},{1,68},{7,97}
        };
        int[][] expected = {{1,93},{7,99}};
        // Student 1 top 5: [100,100,100,100,100] = 500/5 = 100
        // Student 7 top 5: [100,100,100,100,100] = 500/5 = 100
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 1: Exactly 5 scores per student ────────────────────────────

    @Test
    @DisplayName("Edge 1 — exactly 5 scores per student, no extras to skip")
    void testExactlyFiveScores() {
        int[][] items = {
            {1,90},{1,85},{1,80},{1,75},{1,70}
        };
        int[][] expected = {{1,80}};
        // 90+85+80+75+70 = 400 / 5 = 80
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 2: Many scores per student ─────────────────────────────────

    @Test
    @DisplayName("Edge 2 — many scores per student, only top 5 should count")
    void testManyScoresPerStudent() {
        int[][] items = {
            {1,10},{1,20},{1,30},{1,40},{1,50},
            {1,60},{1,70},{1,80},{1,90},{1,100}
        };
        int[][] expected = {{1,80}};
        // Top 5: [100,90,80,70,60] = 400/5 = 80
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 3: All identical scores ────────────────────────────────────

    @Test
    @DisplayName("Edge 3 — all scores identical, no deduplication issues")
    void testAllIdenticalScores() {
        int[][] items = {
            {1,100},{1,100},{1,100},{1,100},{1,100},{1,100}
        };
        int[][] expected = {{1,100}};
        // Top 5: [100,100,100,100,100] = 500/5 = 100
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 4: All zero scores ─────────────────────────────────────────

    @Test
    @DisplayName("Edge 4 — all zero scores")
    void testAllZeroScores() {
        int[][] items = {
            {1,0},{1,0},{1,0},{1,0},{1,0}
        };
        int[][] expected = {{1,0}};
        // 0+0+0+0+0 = 0/5 = 0
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 5: Floor division ──────────────────────────────────────────

    @Test
    @DisplayName("Edge 5a — average is exact integer (no floor needed)")
    void testExactAverageNoFloor() {
        int[][] items = {
            {1,97},{1,96},{1,95},{1,94},{1,93}
        };
        int[][] expected = {{1,95}};
        // 97+96+95+94+93 = 475 / 5 = 95 exactly
        assertArrayEquals(expected, solution.highFive(items));
    }

    @Test
    @DisplayName("Edge 5b — average requires floor (fractional result)")
    void testFloorDivisionRequired() {
        int[][] items = {
            {1,99},{1,98},{1,97},{1,96},{1,91}
        };
        int[][] expected = {{1,96}};
        // 99+98+97+96+91 = 481 / 5 = 96.2 → floor = 96 NOT 97
        assertArrayEquals(expected, solution.highFive(items));
    }

    @Test
    @DisplayName("Edge 5c — floor division with sum = 1 below multiple of 5")
    void testFloorDivisionEdgeBoundary() {
        int[][] items = {
            {1,100},{1,100},{1,100},{1,100},{1,99}
        };
        int[][] expected = {{1,99}};
        // 100+100+100+100+99 = 499 / 5 = 99.8 → floor = 99
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 6: Output sorted by student ID ─────────────────────────────

    @Test
    @DisplayName("Edge 6 — multiple students input unsorted, output must be sorted by ID")
    void testOutputSortedByStudentId() {
        int[][] items = {
            {3,90},{1,85},{2,95},{1,90},{3,80},
            {2,90},{1,70},{3,70},{2,85},{1,60},
            {3,60},{2,80},{1,50},{3,50},{2,70}
        };
        int[][] expected = {{1,71},{2,84},{3,70}};
        // Student 1 top 5: [90,85,70,60,50] = 355/5 = 71
        // Student 2 top 5: [95,90,85,80,70] = 420/5 = 84
        // Student 3 top 5: [90,80,70,60,50] = 350/5 = 70
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 7: Single student ──────────────────────────────────────────

    @Test
    @DisplayName("Edge 7 — single student, result array must have exactly 1 row")
    void testSingleStudent() {
        int[][] items = {
            {7,100},{7,99},{7,98},{7,97},{7,96}
        };
        int[][] expected = {{7,98}};
        // 100+99+98+97+96 = 490 / 5 = 98
        int[][] result = solution.highFive(items);
        assertEquals(1, result.length, "Result should have exactly 1 row");
        assertArrayEquals(expected, result);
    }

    // ─── EDGE CASE 8: Non-consecutive student IDs ─────────────────────────────

    @Test
    @DisplayName("Edge 8 — non-consecutive student IDs (e.g. 1 and 1000)")
    void testNonConsecutiveStudentIds() {
        int[][] items = {
            {1,90},{1,85},{1,80},{1,75},{1,70},
            {1000,100},{1000,99},{1000,98},{1000,97},{1000,96}
        };
        int[][] expected = {{1,80},{1000,98}};
        // Student 1:    90+85+80+75+70 = 400/5 = 80
        // Student 1000: 100+99+98+97+96 = 490/5 = 98
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 9: Minimum score = 0, maximum score = 100 ──────────────────

    @Test
    @DisplayName("Edge 9 — mix of min (0) and max (100) scores")
    void testMinAndMaxScores() {
        int[][] items = {
            {1,100},{1,100},{1,100},{1,100},{1,100},
            {1,0},{1,0},{1,0},{1,0},{1,0}
        };
        int[][] expected = {{1,100}};
        // Top 5 are all 100s → 500/5 = 100
        // Bottom 5 (0s) should be ignored
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 10: Two students same scores different IDs ─────────────────

    @Test
    @DisplayName("Edge 10 — two students with identical scores, different IDs")
    void testTwoStudentsSameScores() {
        int[][] items = {
            {1,90},{1,85},{1,80},{1,75},{1,70},
            {2,90},{2,85},{2,80},{2,75},{2,70}
        };
        int[][] expected = {{1,80},{2,80}};
        // Both students: 90+85+80+75+70 = 400/5 = 80
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── EDGE CASE 11: Scores in worst-case order (ascending per student) ──────

    @Test
    @DisplayName("Edge 11 — scores given in ascending order (worst case for sort)")
    void testScoresInAscendingOrder() {
        int[][] items = {
            {1,50},{1,60},{1,70},{1,80},{1,90},{1,100}
        };
        // Top 5: [100,90,80,70,60] = 400/5 = ... wait
        // 100+90+80+70+60 = 400/5 = 80? No:
        // 100+90+80+70+60 = 400 → 400/5 = 80
        // Hmm let me recalculate: top 5 = [100,90,80,70,60]
        // 100+90+80+70+60 = 400 → 400/5 = 80
        // Actually: 100+90+80+70+60 = 400, 400/5 = 80
        // But let me recount: 100+90=190, +80=270, +70=340, +60=400. Yes 400/5=80.
        // Correction — my earlier comment "88" was wrong. Fix:
        int[][] corrected = {{1,80}};
        // Top 5: [100,90,80,70,60] = 400/5 = 80
        assertArrayEquals(corrected, solution.highFive(items));
    }

    // ─── EDGE CASE 12: Large number of students ───────────────────────────────

    @Test
    @DisplayName("Edge 12 — 10 students each with 10 scores, all scores = 100")
    void testManyStudentsAllMaxScores() {
        int n = 10;
        int scoresPerStudent = 10;
        int[][] items = new int[n * scoresPerStudent][2];
        int idx = 0;
        for (int student = 1; student <= n; student++) {
            for (int s = 0; s < scoresPerStudent; s++) {
                items[idx][0] = student;
                items[idx][1] = 100;
                idx++;
            }
        }
        int[][] result = solution.highFive(items);
        assertEquals(n, result.length, "Should have exactly " + n + " students in result");
        for (int i = 0; i < n; i++) {
            assertEquals(i + 1, result[i][0], "Student IDs should be sorted ascending");
            assertEquals(100, result[i][1], "Average should be 100");
        }
    }

    // ─── EDGE CASE 13: Student with scores just above/below average boundary ───

    @Test
    @DisplayName("Edge 13 — 6 scores where dropping the lowest changes average noticeably")
    void testDroppingLowestScoreMatters() {
        int[][] items = {
            {1,100},{1,100},{1,100},{1,100},{1,100},{1,0}
        };
        int[][] expected = {{1,100}};
        // Top 5: [100,100,100,100,100] = 500/5 = 100
        // The 0 must be excluded
        assertArrayEquals(expected, solution.highFive(items));
    }

    @Test
    @DisplayName("Edge 13b — 6 scores where the 6th score is just below top 5")
    void testSixthScoreJustBelowTopFive() {
        int[][] items = {
            {1,100},{1,90},{1,80},{1,70},{1,60},{1,59}
        };
        int[][] expected = {{1,80}};
        // Top 5: [100,90,80,70,60] = 400/5 = 80
        // Score of 59 must be excluded
        assertArrayEquals(expected, solution.highFive(items));
    }

    // ─── RESULT STRUCTURE TESTS ───────────────────────────────────────────────

    @Test
    @DisplayName("Structure — each result row has exactly 2 elements")
    void testResultRowLength() {
        int[][] items = {
            {1,90},{1,85},{1,80},{1,75},{1,70}
        };
        int[][] result = solution.highFive(items);
        for (int[] row : result) {
            assertEquals(2, row.length, "Each result row must have exactly [studentId, average]");
        }
    }

    @Test
    @DisplayName("Structure — result length equals number of unique students")
    void testResultLengthEqualsUniqueStudents() {
        int[][] items = {
            {1,90},{1,85},{1,80},{1,75},{1,70},
            {2,90},{2,85},{2,80},{2,75},{2,70},
            {3,90},{3,85},{3,80},{3,75},{3,70}
        };
        int[][] result = solution.highFive(items);
        assertEquals(3, result.length, "Result must have one row per unique student");
    }
}