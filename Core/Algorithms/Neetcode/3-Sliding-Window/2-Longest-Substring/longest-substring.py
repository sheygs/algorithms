"""
Time Complexity: O(n * m)
Space Complexity: O(m)
Where 'n' is the length of the string and
'm' is the total number of unique characters in the string
"""

# def lengthOfLongestSubstringBrute(s: str):
#     res = 0
#     for i in range(len(s)):
#         charSet = set()
#         for j in range(i, len(s)):
#             if s[j] in charSet:
#                 break
#             charSet.add(s[j])
#         res = max(res, len(charSet))
#     return res


"""
    Sliding Window
    TC: O(n)
    SC: O(m)
    'n' is the length of the string and
    'm' is the total number of unique characters in the string
"""


def lengthOfLongestSubstring(s: str) -> int:
    charSet = set()  # tracks unique chars in current window
    left = 0  # left pointer
    maxLength = 0  # best result so far

    for right in range(len(s)):  # r is the right pointer, always moves forward
        while s[right] in charSet:  # duplicate found — window is invalid
            charSet.remove(s[left])  # remove the leftmost char
            left += 1  # shrink window from the left
        charSet.add(s[right])  # safe to add — no duplicate now
        maxLength = max(maxLength, right - left + 1)  # window size = r - l + 1
    return maxLength


# print(lengthOfLongestSubstring("abcabcbb"))
# print(lengthOfLongestSubstring("zxyzxyz"))
# print(lengthOfLongestSubstring("xxxx"))
# print(lengthOfLongestSubstring("pwwkew"))
