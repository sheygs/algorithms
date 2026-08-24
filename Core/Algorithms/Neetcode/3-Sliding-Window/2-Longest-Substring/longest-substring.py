# Sliding Window


# Time Complexity:
# Space Complexity:
def lengthOfLongestSubstring():
    pass


# Time Complexity: O(n)
# Space Complexity: O(m)
def lengthOfLongestSubstringOptimal(s: str) -> int:
    charSet = set()  # tracks unique chars in current window
    l = 0  # left pointer
    res = 0  # best result so far

    for r in range(len(s)):  # r is the right pointer, always moves forward
        while s[r] in charSet:  # duplicate found — window is invalid
            charSet.remove(s[l])  # remove the leftmost char
            l += 1  # shrink window from the left
        charSet.add(s[r])  # safe to add — no duplicate now
        res = max(res, r - l + 1)  # window size = r - l + 1
    return res


print(lengthOfLongestSubstringOptimal("abcabcbb"))
# print(lengthOfLongestSubstringOptimal("zxyzxyz"))
# print(lengthOfLongestSubstringOptimal("xxxx"))
# print(lengthOfLongestSubstringOptimal("pwwkew"))
