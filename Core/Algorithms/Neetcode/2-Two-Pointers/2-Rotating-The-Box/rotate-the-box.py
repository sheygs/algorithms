from typing import List

"""
    stone - '#'
    obstacle - '*'
    empty - '.'
"""


# Brute Force
# Time Complexity: O(m*n^2)
# Space Complexity:  O(m*n)
def rotateTheBox(boxGrid: List[List[str]]) -> List[List[str]]:
    pass


# Optimal
# Time Complexity: O(m*n)
# Space Complexity: O(m*n)
def rotateTheBoxOptimal(boxGrid: List[List[str]]) -> List[List[str]]:
    ROWS, COLS = len(boxGrid), len(boxGrid[0])

    for r in range(ROWS):
        # last position in the row
        # pointer to the next available landing spot
        i = COLS - 1

        for c in reversed(range(COLS)):  # scan right to left
            # print(boxGrid[r][c])
            if boxGrid[r][c] == "#":  # stone found
                boxGrid[r][c], boxGrid[r][i] = (
                    boxGrid[r][i],
                    boxGrid[r][c],
                )  # moves a stone from its current position `c` to the rightmost available landing spot `i`
                i -= 1  # next landing spot moves left
            elif boxGrid[r][c] == "*":  # obstacle found
                i = c - 1  # reset landing spot to just left of obstacle

    result = []

    # already rotated
    for c in range(COLS):  # each original column becomes a new row
        column = []  # row after rotation
        for r in reversed(range(ROWS)):  # read original rows bottom to top
            column.append(boxGrid[r][c])
        result.append(column)

    return result


print(rotateTheBoxOptimal([["#", ".", "#"]]))
# print(rotateTheBoxOptimal([["#", ".", "*", "."], ["#", "#", "*", "."]]))
# print(
#     rotateTheBoxOptimal(
#         [
#             ["#", "#", "*", ".", "*", "."],
#             ["#", "#", "#", "*", ".", "."],
#             ["#", "#", "#", ".", "#", "."],
#         ]
#     )
# )
