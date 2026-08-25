# 1086. High Five

## Difficulty

Easy

## Problem Statement

Given a list of the scores of different students, `items`, where `items[i] = [IDi, scorei]`
represents one score from a student with `IDi`, calculate each student's **top five average**.

Return the answer as an array of pairs `result`, where `result[j] = [IDj, topFiveAveragej]`
represents the student with `IDj` and their **top five average**. Sort `result` by `IDj` in
**increasing order**.

A student's **top five average** is calculated by taking the sum of their top five scores and
dividing it by `5` using **integer division**.

---

## Examples

**Example 1:**

```text
Input: items = [[1,91],[1,92],[2,93],[2,97],[1,60],[2,77],[1,65],[1,87],[1,100],[2,100],[2,76]]
Output: [[1,87],[2,88]]
Explanation:
The student with ID = 1 got scores 91, 92, 60, 65, 87, and 100.
Their top five average is (100 + 92 + 91 + 87 + 65) / 5 = 87.
The student with ID = 2 got scores 93, 97, 77, 100, and 76.
Their top five average is (100 + 97 + 93 + 77 + 76) / 5 = 88.
```

**Example 2:**

```text
Input: items = [[1,100],[7,100],[1,100],[7,100],[1,100],[7,100],[1,100],[7,100],[1,100],[7,100]]
Output: [[1,100],[7,100]]
```

---

## Constraints

- `1 <= items.length <= 1000`
- `items[i].length == 2`
- `1 <= IDi <= 1000`
- `0 <= scorei <= 100`
- For each `IDi`, there will be **at least** 5 scores.

---

## Topics

`Array` `Hash Table` `Sorting` `Heap (Priority Queue)`

## Efficient Computation

## Time Complexity — O(n log n)

| Line                           | Cost              | Reason                                          |
| ------------------------------ | ----------------- | ----------------------------------------------- |
| Building `scores` map          | O(n)              | Single pass through all `n` items               |
| `sorted(scores)`               | O(k log k)        | Sorting `k` unique student IDs                  |
| `nlargest(5, ...)` per student | O(n log 5) = O(n) | Across all students, total scores processed = n |
| `sum(top5)` per student        | O(1)              | Always exactly 5 elements                       |
| **Total**                      | **O(n log n)**    | Sorting dominates when k is close to n          |

> `k` = number of unique students, `n` = total number of score entries. In the worst case every entry belongs to a different student so `k → n`, making `sorted` cost O(n log n).

---

## Space Complexity — O(n)

| Structure        | Cost     | Reason                                           |
| ---------------- | -------- | ------------------------------------------------ |
| `scores` hashmap | O(n)     | Stores all `n` scores across all students        |
| `result` array   | O(k)     | One entry per unique student                     |
| `top5` list      | O(1)     | Always exactly 5 elements, reused each iteration |
| **Total**        | **O(n)** | Hashmap dominates                                |

---

## Final Answer

```test
Time:  O(n log n)
Space: O(n)
```

## Concrete Numbers

Say a student has **1000 scores** and you need top 5:

```test
sorted:    1000 × log(1000) ≈ 1000 × 10  = 10,000 operations
nlargest:  1000 × log(5)    ≈ 1000 × 2.3 =  2,300 operations
```
