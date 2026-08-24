# Rotating the Box

**Difficulty:** Medium · **Company:** Visa · **Topics:** Arrays, Two Pointers, Matrix

---

## Problem

You are given an `m x n` matrix of characters `boxGrid` representing a side-view of a box. Each cell of the box is one of the following:

| Symbol | Meaning               |
| ------ | --------------------- |
| `#`    | A stone               |
| `*`    | A stationary obstacle |
| `.`    | Empty space           |

The box is rotated **90 degrees clockwise**, causing some of the stones to fall due to gravity. Each stone falls down until it lands on an obstacle, another stone, or the bottom of the box.

> **Note:** Gravity does not affect obstacle positions, and the inertia from the rotation does not affect stones' horizontal positions. Each stone in `boxGrid` is guaranteed to rest on an obstacle, another stone, or the bottom of the box.

Return an `n x m` matrix representing the box after the rotation.

---

## Examples

### Example 1

**Input:**

```python
boxGrid = [
    ["#", ".", "*", "."],
    ["#", "#", "*", "."]
]
```

**Output:**

```python
[
    ["#", "."],
    ["#", "#"],
    ["*", "*"],
    [".", "."]
]
```

---

### Example 2

**Input:**

```python
boxGrid = [
    ["#", "#", "*", ".", "*", "."],
    ["#", "#", "#", "*", ".", "."],
    ["#", "#", "#", ".", "#", "."]
]
```

**Output:**

```python
[
    [".", "#", "#"],
    [".", "#", "#"],
    ["#", "#", "*"],
    ["#", "*", "."],
    ["#", ".", "*"],
    ["#", ".", "."]
]
```

---

## Constraints

- `m == boxGrid.length`
- `n == boxGrid[i].length`
- `1 <= m, n <= 500`
- `boxGrid[i][j]` is either `'#'`, `'*'`, or `'.'`
