from collections import defaultdict
from typing import List

"""
Time: O(m*nlogn)
Space: O(m*n)
"""

# def groupAnagramsBrute(strs: List[str]) -> List[List[str]]:
#     res = defaultdict(list)
#     for s in strs:
#         sortedS = "".join(sorted(s))
#         res[sortedS].append(s)
#     return list(res.values())


"""
Time: O(m*n)
Space: O(m) extra space


    {
        (1,0,1,0...): ["ace",...],
        ...
    }

"""


# TC: O(m*n), SC: O(m*n)
def groupAnagrams(strs: List[str]) -> List[List[str]]:
    counter = defaultdict(list)

    for s in strs:
        count = [0] * 26

        for char in s:

            count[ord(char) - ord("a")] += 1

        counter[tuple(count)].append(s)

    return list(counter.values())


print(groupAnagrams(["eat", "tea", "tan", "ate", "nat", "bat"]))
