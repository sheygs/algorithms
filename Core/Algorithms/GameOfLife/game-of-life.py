from typing import List


# LC 289
# Time Complexity:  O(m×n) — every cell visited twice (one pass to mark, one to finalise)
# m is the number of rows, n is the number of columns
# Space Complexity: O(1)   — no extra board, states encoded in-place


def gameOfLife(board: List[List[int]]) -> None:
    # In-place state encoding
    # Original | New | State | Meaning
    # 0        | 0   |   0   | was dead, stay dead
    # 1        | 0   |   1   | was alive, will die
    # 0        | 1   |   2   | was dead,  will be born
    # 1        | 1   |   3   | was alive, stays alive

    # encoding: original value and new value are packed into a single integer
    # State 0: original=0, new=0  → dead  stays dead
    # State 1: original=1, new=0  → alive will die
    # State 2: original=0, new=1  → dead  will be born
    # State 3: original=1, new=1  → alive stays alive
    # key insight: odd states (1,3) = was originally alive
    #              even states (0,2) = was originally dead

    rows, cols = len(board), len(board[0])

    def count_live_neighbours(r, c):
        count = 0

        # iterate over a 3×3 grid centred on (r, c)
        # r-1 to r+1 covers the row above, current row, row below
        # c-1 to c+1 covers the column left, current col, column right
        for i in range(r - 1, r + 2):  # r+2 because range end is exclusive
            for j in range(c - 1, c + 2):

                # skip this cell entirely if any of these are true:
                if i == r and j == c:  # 1. it's the cell itself, not a neighbour
                    continue
                if i < 0 or j < 0:  # 2. out of bounds on the top or left
                    continue
                if i == rows or j == cols:  # 3. out of bounds on the bottom or right
                    continue

                # check if this neighbour WAS alive in the original board
                # with this encoding, original value was 1 if current value is 1 or 3
                #   1 → was alive, will die    (odd → was alive ✅)
                #   3 → was alive, stays alive (odd → was alive ✅)
                #   0 → was dead, stays dead   (even → was dead ❌)
                #   2 → was dead, will be born (even → was dead ❌)
                # print((i, j))
                if board[i][j] in (1, 3):
                    count += 1

        return count

    # ── Pass 1: mark transitions ─────────────────────────────────────────────
    # O(m*n)
    for r in range(rows):
        for c in range(cols):
            lives = count_live_neighbours(r, c)

            if board[r][c] == 1:
                # cell is currently alive
                # rule 2 & 3: survives with 2 or 3 neighbours → mark as 3 (alive→alive)
                if lives in [2, 3]:
                    board[r][c] = 3
                # rule 1 & 4: dies with < 2 or > 3 neighbours → stays as 1 (alive→dead)
                # no change needed — 1 already encodes "was alive, will die"

            else:
                # cell is currently dead (board[r][c] == 0)
                # rule 4: exactly 3 live neighbours → born → mark as 2 (dead→alive)
                if lives == 3:
                    board[r][c] = 2
                # otherwise stays dead → stays as 0, no change needed

    # ── Pass 2: finalise all cells to 0 or 1 ────────────────────────────────
    # O(m*n)
    for r in range(rows):
        for c in range(cols):
            # state 1 means "was alive, will die" → becomes 0
            if board[r][c] == 1:
                board[r][c] = 0

            # states 2 and 3 both mean the new value is 1 (alive)
            # state 2: was dead, now born   → 1
            # state 3: was alive, survived  → 1
            elif board[r][c] in [2, 3]:
                board[r][c] = 1


# ── Tests ────────────────────────────────────────────────────────────────────
board1 = [[0, 1, 0], [0, 0, 1], [1, 1, 1], [0, 0, 0]]
gameOfLife(board1)
print(board1)  # [[0,0,0],[1,0,1],[0,1,1],[0,1,0]] ✅

board2 = [[1, 1], [1, 0]]
gameOfLife(board2)
print(board2)  # [[1,1],[1,1]] ✅
