# Fraction to Recurring Decimal

## Problem

Given two integers representing the **numerator** and **denominator** of a fraction, return the fraction in **string format**.

If the fractional part is **repeating**, enclose the repeating part in **parentheses**.

If multiple answers are possible, return **any of them**.

If the fraction can be represented as a **finite decimal**, return it normally without parentheses.

It is guaranteed that the length of the result string is less than `10^4`.

---

## Examples

### Example 1

```text
Input: numerator = 1, denominator = 2
Output: "0.5"
```

---

### Example 2

```text
Input: numerator = 2, denominator = 1
Output: "2"
```

---

### Example 3

```text
Input: numerator = 4, denominator = 333
Output: "0.(012)"
```

Explanation:

```text
4 / 333 = 0.012012012...
```

The repeating sequence `012` is enclosed in parentheses.

---

## Constraints

```text
-2^31 <= numerator, denominator <= 2^31 - 1
denominator != 0
```

---

## Approach

To convert a fraction to a decimal:

1. **Determine the sign**
   - If numerator and denominator have different signs, the result is negative.

2. **Compute the integer part**

   ```text
   integer_part = numerator // denominator
   ```

3. **Compute the remainder**

   ```text
   remainder = numerator % denominator
   ```

4. If the remainder is `0`, the fraction is **finite**, so return the integer part.

5. Otherwise, compute the **decimal part** using **long division**.

6. Use a **hash map** to store previously seen remainders and their positions in the result string.

7. If a remainder repeats:
   - The digits between the two occurrences form the **repeating sequence**.
   - Insert parentheses around that sequence.

---

## Algorithm

1. Handle the sign.
2. Convert numerator and denominator to absolute values.
3. Compute the integer part.
4. If remainder is `0`, return result.
5. Append `"."` to result.
6. Use a dictionary to track remainders.
7. While remainder is not `0`:
   - If remainder already exists in dictionary:
     - Insert `"("` at the stored position.
     - Append `")"` and stop.
   - Store remainder position.
   - Multiply remainder by `10`.
   - Append `(remainder // denominator)` to result.
   - Update remainder.

---

## Complexity Analysis

### Time Complexity

```text
O(n)
```

Where `n` is the length of the repeating cycle.

---

### Space Complexity

```text
O(n)
```

Used for storing remainders in the hash map.

---

## Key Idea

During **long division**, if a remainder repeats, the digits between the two occurrences form the **repeating decimal sequence**.

Example:

```text
4 / 333

Step 1: 4 * 10 = 40 → 0
Step 2: 40 * 10 = 400 → 1
Step 3: 67 * 10 = 670 → 2
```

The remainder repeats, producing:

```text
0.(012)
```

---

## Edge Cases

### Zero numerator

```text
0 / 5 → "0"
```

### Negative result

```text
-1 / 2 → "-0.5"
```

### Large numbers

Handled safely using absolute values and integer arithmetic.

---

## Key Takeaway

This problem combines:

- **Math (long division)**
- **Hash table for cycle detection**
- **String construction**

Detecting **repeated remainders** allows us to identify the **recurring decimal pattern** efficiently.
