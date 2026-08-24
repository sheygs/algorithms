from typing import List

"""
Time: O(n)
Space: O(n)
"""


def compressBrute(chars: List[str]) -> int:
    pass


"""
Time: O(n)
Space: O(1)
"""


# step 1: Identify Groups of Consecutive Characters
def bit1_identify_groups(chars: List[str]):
    """
    Scan through chars and print each group.
    A group ends when the character changes or we hit the end.
    """
    i = 0
    n = len(chars)
    groups = []

    while i < n:

        current_char = chars[i]
        group_start = i

        while i < n and chars[i] == current_char:
            i += 1

        group = chars[group_start:i]

        print(
            f"Group: '{current_char}' × {len(group)}  →  indices [{group_start}..{i-1}]"
        )

        groups.append(group)

    return groups


# print(bit1_identify_groups(["a", "a", "b", "b", "c", "c", "c"]))


# Bit 2 — Count Each Group's Length


def bit2_count_groups(chars):
    """
    For each group, compute its length (count).
    Count is used to decide what to write after the character.
    """

    n = len(chars)
    i = 0

    groups = []

    while i < n:

        current_char = chars[i]
        # group_start = i
        count = 0

        while i < n and chars[i] == current_char:
            i += 1
            count += 1

        # group = chars[group_start:i]
        groups.append((current_char, count))

    return groups


# print(bit2_count_groups(["a", "a", "b", "b", "c", "c", "c"]))

# Bit 3 — Write the Character In-Place


def bit3_count_groups(chars):
    """
    Write just the character (not the count yet) at the write pointer.
    Shows how write pointer stays behind or equal to i.
    """

    n = len(chars)
    i = 0
    # groups = []
    write = 0

    while i < n:
        current_char = chars[i]
        count = 0

        while i < n and chars[i] == current_char:
            i += 1
            count += 1

        # groups.append((current_char, count))
        chars[write] = current_char
        print(
            f"Wrote '{current_char}' at index {write}  (count={count}, write pointer was {write})"
        )
        write += 1

    print(f"\nchars so far: {chars}")
    print(f"write pointer ended at: {write}")
    return write


# print(bit3_count_groups(["a", "a", "b", "b", "c", "c", "c"]))


# Bit 4  - Write the Count Digits (The Tricky Part)
def bit4_count_groups(chars):
    """
    After writing the character, write each digit of count if count > 1.
    str(count) splits multi-digit counts: 12 → ['1', '2']
    This satisfies the constraint that 10+ groups split into multiple chars.
    """
    n = len(chars)
    i = 0
    write = 0
    # groups = []

    while i < n:
        current_char = chars[i]
        count = 0

        while i < n and chars[i] == current_char:
            i += 1
            count += 1

        # groups.append((current_char, count))

        chars[write] = current_char
        write += 1

        # print(count)

        if count > 1:
            for digit in str(count):
                print("digits: ", digit)
                chars[write] = digit
                print(f"  Wrote digit '{digit}' at index {write}")
                write += 1

    print(f"\nResult: {chars[:write]}  length={write}")
    return write


# print(bit4_count_groups(["a", "a", "b", "b", "c", "c", "c"]))

"""
Full Solution
"""

# Time Complexity:  O(n) — i scans every character exactly once
# Space Complexity: O(1) — only pointers and digit writes, no extra structures
#                          str(count) uses O(log count) space but count ≤ n,
#                          so at most O(log n) — treated as O(1) in practice


def compress(chars: List[str]) -> int:
    i = 0  # READ pointer  — scans through chars finding groups
    write = 0  # WRITE pointer — overwrites chars in-place with compressed output
    n = len(chars)

    while i < n:
        current_char = chars[i]
        count = 0

        # count the full length of this consecutive group
        while i < n and chars[i] == current_char:
            i += 1
            count += 1

        # always write the character itself
        chars[write] = current_char
        write += 1

        # only write the count if group length > 1
        # str(count) handles multi-digit counts: 12 → '1','2'
        if count > 1:
            for digit in str(count):
                chars[write] = digit
                write += 1

    # write pointer now equals the new length of the compressed array
    return write


# print(compress(["a", "a", "b", "b", "c", "c", "c"]))
# print(compress(["a", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b"]))
# print(compress(["a"]))


# TC: O(n), SC:O(1))
def compress_(chars: List[str]):
    i = 0
    n = len(chars)
    write = 0

    while i < n:
        count = 0
        current_char = chars[i]

        while i < n and chars[i] == current_char:
            i += 1
            count += 1

        chars[write] = current_char
        write += 1

        if count > 1:
            for digit in str(count):
                chars[write] = digit
                write += 1
    return write


print(compress_(["a", "a", "b", "b", "c", "c", "c"]))  # a2b2c3
print(
    compress_(["a", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b", "b"])
)  # a1b11
print(compress_(["a"]))
