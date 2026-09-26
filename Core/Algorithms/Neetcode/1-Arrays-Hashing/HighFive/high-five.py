from typing import List
from collections import defaultdict
from heapq import nlargest


def highFiveBrute(items: List[List[int]]) -> List[List[int]]:
    pass


"""
Time Complexity:  O(n log n)
Space Complexity: O(n)
"""


def highFive(items: List[List[int]]) -> List[List[int]]:
    scores = defaultdict(list)

    result = []

    # group student scores by ID
    for student_id, score in items:  # O(n)
        scores[student_id].append(score)

    # returns student ID in asc order
    for student_id in sorted(scores):  # O(k log k)
        # grab top 5 per student
        # nlargest — only tracks the best 5 using a min-heap
        # whereas sorted — sorts the ENTIRE list, then slices
        top5 = nlargest(5, scores[student_id])  # O(n log 5) = O(n)
        # top5 = sorted(scores[student_id], reverse=True)[:5]
        # compute the average
        avg = sum(top5) // 5  # O(1)
        result.append([student_id, avg])

    return result


# print(
#     highFive(
#         [
#             [1, 91],
#             [1, 92],
#             [2, 93],
#             [2, 97],
#             [1, 60],
#             [2, 77],
#             [1, 65],
#             [1, 87],
#             [1, 100],
#             [2, 100],
#             [2, 76],
#         ]
#     )
# )

# print(
#     highFive(
#         [
#             [1, 100],
#             [7, 100],
#             [1, 100],
#             [7, 100],
#             [1, 100],
#             [7, 100],
#             [1, 100],
#             [7, 100],
#             [1, 100],
#             [7, 100],
#         ]
#     )
# )


# TC: O(nlogn), SC: O(n)
def averageScores(items: List[List[int]]):
    scores = defaultdict(list)
    result = []

    # group scores by ID
    for student_id, score in items:
        scores[student_id].append(score)

    for student_id, scores in scores.items():
        average = sum(scores) // len(scores)
        result.append([student_id, average])

    return result


print("average scores: ",
    averageScores(
        [
            [1, 91],
            [1, 92],
            [2, 93],
            [2, 97],
            [1, 60],
            [2, 77],
            [1, 65],
            [1, 87],
            [1, 100],
            [2, 100],
            [2, 76],
        ]
    )
)
