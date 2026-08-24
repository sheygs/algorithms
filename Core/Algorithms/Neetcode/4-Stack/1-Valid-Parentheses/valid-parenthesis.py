# Time Complexity:  O(n) — single pass through the string
# Space Complexity: O(n) — stack holds at most n/2 opening brackets
def isValid(s: str) -> bool:
    stack = []
    matching = {")": "(", "]": "[", "}": "{"}

    for char in s:
        if char in matching:
            # closing bracket — check if top of stack matches
            # if stack is empty, use '#' as a dummy that never matches
            top = stack.pop() if stack else "#"

            if matching[char] != top:
                return False
        else:
            # opening bracket — push onto stack
            stack.append(char)

    # valid only if all opening brackets were matched and popped
    return len(stack) == 0


print(isValid("()[]{}"))
print(isValid("(]"))
print(isValid("([])"))
