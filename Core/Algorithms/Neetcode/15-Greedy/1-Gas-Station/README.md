# Gas Station

## Difficulty

Medium

## Problem Statement

There are `n` gas stations along a circular route, where the amount of gas at the `i`-th
station is `gas[i]`.

You have a car with an unlimited gas tank and it costs `cost[i]` of gas to travel from the
`i`-th station to its next `(i + 1)`-th station. You begin the journey with an empty tank at
one of the gas stations.

Given two integer arrays `gas` and `cost`, return the starting gas station's index if you can
travel around the circuit once in the clockwise direction, otherwise return `-1`. If there
exists a solution, it is guaranteed to be unique.

---

## Examples

**Example 1:**

```text
Input: gas  = [1,2,3,4,5]
       cost = [3,4,5,1,2]
Output: 2
Explanation: Start at station 2 (index 2) and fill up with 3 units of gas.
             Travel to station 3. Your tank = 0 - 5 + 4 = -1 which is not valid.

             Start at station 2 (index 2) and fill up with 3 units of gas.
             Travel to station 3. Your tank = 0 + 3 - 5 = -2, which is not valid.

             Try station 2. tank = 0 + 3 = 3. Travel to station 3: tank = 3 - 5 = -2.
             That does not work either.

             Start at station 3 (index 3):
             tank = 0 + 4 - 1 = 3
             tank = 3 + 5 - 2 = 6
             tank = 6 + 1 - 3 = 4
             tank = 4 + 2 - 4 = 2
             tank = 2 + 3 - 5 = 0
             Return to starting station 3. Total gas = 15, total cost = 11.
             We can travel around the circuit. So return 2.
```

**Example 2:**

```text
Input: gas  = [2,3,4]
       cost = [3,4,3]
Output: -1
Explanation: You can't start at station 0 or 1, as there is not enough gas to travel to the
             next station. Let's start at station 2 and fill up with 4 units of gas.
             Your tank = 0 + 4 - 3 = 1. Travel to station 0. Your tank = 1 + 2 - 3 = 0.
             Travel to station 1. Your tank = 0 + 3 - 4 = -1. Not valid.
```

---

## Constraints

- `n == gas.length == cost.length`
- `1 <= n <= 10^5`
- `0 <= gas[i] <= 10^4`
- `0 <= cost[i] <= 10^4`

---

## Topics

`Array` `Greedy`

## Visual: <https://www.youtube.com/watch?v=9h-2YiTNam4>
