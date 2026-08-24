"""
Time Complexity: O(n^2)
Space Complexity: O(1)
"""

from collections import defaultdict


# def firstUniqCharBrute(s: str) -> int:
#     # if not s:
#     #     return

#     for i in range(len(s)):
#         flag = True
#         for j in range(len(s)):
#             if i == j:
#                 continue
#             if s[i] == s[j]:
#                 flag = False
#                 break
#         if flag:
#             return i
#     return -1


"""
Time Complexity: O(n)
Space Complexity: O(1) The hash map size is capped by the number of unique characters in the alphabet (e.g., 26).
"""


def firstUniqChar(s: str) -> int:
    if not s:
        return -1

    count = defaultdict(int)

    for char in s:
        count[char] += 1

    for index, char in enumerate(s):
        if count[char] == 1:
            return index

    return -1


# print(firstUniqChar("leetcode"))
# print(firstUniqChar("loveleetcode"))
# print(firstUniqChar(" aabb "))
# print(firstUniqChar(""))


# TODO: find out why SC is O(1) and not O(n)
# TC: O(n) , SC: O(1)
def firstUniqCharTest(str: str):
    if not str:
        return -1

    str = str.strip()  # O(n)

    counter = defaultdict(int)

    for char in str:
        # counter[char] = counter.get(char, 0) + 1
        counter[char] += 1

    for index, char in enumerate(str):
        if counter[char] == 1:
            return index
    return -1


print(firstUniqCharTest("leetcode"))
print(firstUniqCharTest("loveleetcode"))
print(firstUniqCharTest(" aabb "))
print(firstUniqChar(""))
