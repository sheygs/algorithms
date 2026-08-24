# Number of Islands

**Difficulty:** Medium

---

## Topics

Array, Depth-First Search, Breadth-First Search, Union Find, Matrix

---

## Problem Statement

Given a 2D grid `grid` where `'1'` represents land and `'0'` represents water, count and return the number of islands.

An **island** is formed by connecting adjacent lands horizontally or vertically and is surrounded by water. You may assume water is surrounding the grid (i.e., all the edges are water).

---

## Examples

**Example 1:**

```text
Input: grid = [
  ["0","1","1","1","0"],
  ["0","1","0","1","0"],
  ["1","1","0","0","0"],
  ["0","0","0","0","0"]
]
Output: 1
```

---

**Example 2:**

```text
Input: grid = [
  ["1","1","0","0","1"],
  ["1","1","0","0","1"],
  ["0","0","1","0","0"],
  ["0","0","0","1","1"]
]
Output: 4
```

---

## Constraints

- `1 <= grid.length, grid[i].length <= 100`
- `grid[i][j]` is `'0'` or `'1'`
