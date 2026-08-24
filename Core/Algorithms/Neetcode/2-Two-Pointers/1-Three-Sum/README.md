# 3Sum

**Difficulty:** Medium
**Company:** Visa
**Topics:** Arrays, Two Pointers

---

## 📝 Problem Statement

Given an integer array `nums`, return **all unique triplets** `[nums[i], nums[j], nums[k]]` such that:

- `i != j`
- `i != k`
- `j != k`
- `nums[i] + nums[j] + nums[k] == 0`

> ⚠️ The solution set **must not contain duplicate triplets**.

---

### 🔍 Examples

#### Example 1

**Input:**

```text
nums = [-1, 0, 1, 2, -1, -4]
```

**Output:**

```text
[[-1, -1, 2], [-1, 0, 1]]
```

**Explanation:**

- `(-1) + 0 + 1 = 0`
- `(-1) + (-1) + 2 = 0`

The distinct triplets are:

- `[-1, 0, 1]`
- `[-1, -1, 2]`

> The order of the output and the order of the triplets does not matter.

---

#### Example 2

**Input:**

```text
nums = [0, 1, 1]
```

**Output:**

```text
[]
```

**Explanation:**
No combination of three numbers sums to `0`.

---

#### Example 3

**Input:**

```text
nums = [0, 0, 0]
```

**Output:**

```text
[[0, 0, 0]]
```

**Explanation:**
The only possible triplet sums to `0`.

---

### 📌 Constraints

- `3 <= nums.length <= 3000`
- `-10^5 <= nums[i] <= 10^5`

---
