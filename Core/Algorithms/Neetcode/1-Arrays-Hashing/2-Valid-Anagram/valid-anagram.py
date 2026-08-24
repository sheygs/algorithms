# Time Complexity: O(nlogn + mlogm)
# Space Complexity: O(1)
# sorting
def isAnagramSorting(s: str, t: str) -> bool:
    if len(s) != len(t):
        return False
    return sorted(s) == sorted(t)


# Time Complexity: O(n + m)
# Space Complexity: O(1)
# Hashmap
def isAnagram(s: str, t: str) -> bool:
    if len(s) != len(t):
        return False

    countS, countT = {}, {}

    for i in range(len(t)):
        countS[s[i]] = 1 + countS.get(s[i], 0)
        countT[t[i]] = 1 + countT.get(t[i], 0)

    # compare both maps
    return countS == countT

