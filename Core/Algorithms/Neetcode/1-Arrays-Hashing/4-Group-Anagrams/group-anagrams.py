from collections import defaultdict
from typing import List


# Time Complexity:
# Space Complexity:
def groupAnagrams():
    pass


# Time Complexity: O(m * n) — m = number of strings, n = average string length
# Space Complexity:  O(m * n) — storing all strings in the hashmap
def groupAnagramsOptimal(strs: List[str]) -> List[List[str]]:
    # default dict auto-initialises missing keys as empty lists
    # key: tuple of 26 character counts, value: list of anagram strings

    """
    {
        (1,0,1,0...): [],
        ...
    }
    """
    res = defaultdict(list)

    for s in strs:
        # one slot per letter a-z, all starting at zero
        count = [0] * 26
        for c in s:
            # map character to index 0-25:  'a'->0, 'b'->1, ..., 'z'->25
            # then increment that letter's frequency slot
            count[ord(c) - ord("a")] += 1

        # lists are not hashable so convert to tuple to use as dict key
        # anagrams always produce the same frequency tuple → same bucket
        res[tuple(count)].append(s)

    #  return all grouped anagram lists, discarding the keys
    return list(res.values())


print(groupAnagramsOptimal(["eat", "tea", "tan", "ate", "nat", "bat"]))
# print(groupAnagramsOptimal([""]))
# print(groupAnagramsOptimal(["a"]))


# def groupAnagrams(strs: List[str]) -> List[List[str]]:

#     result = {}

#     for s in strs:
#         count = [0] * 26
#         for c in s:
#             count[ord(c) - ord("a")] += 1

#         if tuple(count) not in result:
#             result[tuple(count)] = []
#         result[tuple(count)].append(s)

#     return list(result.values())
