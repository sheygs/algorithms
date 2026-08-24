# Robot Return to Origin

**Difficulty:** Easy

## Problem

There is a robot starting at position `(0, 0)` on a 2D plane. Given a string `moves` representing the robot's movement sequence, return `true` if the robot returns to the origin `(0, 0)` after executing all moves, otherwise return `false`.

Each character in `moves` is one of `'U'`, `'D'`, `'L'`, `'R'` — representing up, down, left, and right respectively.

---

## Examples

**Example 1:**

```text
Input:  moves = "UD"
Output: true
Explanation: The robot moves up once, then down once, returning to the origin.
```

**Example 2:**

```text
Input:  moves = "LL"
Output: false
Explanation: The robot moves left twice, ending at (-2, 0).
```

## Constraints

- `1 <= moves.length <= 2 * 10⁴`
- `moves` only contains the characters `'U'`, `'D'`, `'L'`, and `'R'`

## Approach — Coordinate Tracking

Track the robot's `x` and `y` coordinates as it processes each move:

- `'U'` → `y += 1`
- `'D'` → `y -= 1`
- `'L'` → `x -= 1`
- `'R'` → `x += 1`

After all moves, return `true` if and only if `x == 0 and y == 0`.

**Key insight:** The robot returns to the origin if and only if the number of `'U'`s equals the number of `'D'`s, **and** the number of `'L'`s equals the number of `'R'`s.

**Time Complexity:** O(n) — single pass through the string
**Space Complexity:** O(1) — only two integer counters

## Topics

`String` `Simulation`
