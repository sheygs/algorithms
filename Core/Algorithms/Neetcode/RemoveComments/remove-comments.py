from typing import List

# Time Complexity:  O(m×n) — every character visited exactly once
# Space Complexity: O(m×n) — storing the resulting cleaned lines


def removeComments(source: List[str]) -> List[str]:
    result = []
    current_line = []  # characters accumulated for the current output line
    in_block = False  # whether we are currently inside a /* block comment */

    for line in source:
        i = 0
        while i < len(line):

            # ── inside a block comment ───────────────────────────────────────
            if in_block:
                # look for the closing */ marker
                if line[i] == "*" and i + 1 < len(line) and line[i + 1] == "/":
                    in_block = False  # block comment ends
                    i += 2  # skip both '*' and '/'
                else:
                    i += 1  # still inside block, skip character

            # ── normal mode ──────────────────────────────────────────────────
            else:
                # check for block comment opening /*
                if line[i] == "/" and i + 1 < len(line) and line[i + 1] == "*":
                    in_block = True  # enter block comment mode
                    i += 2  # skip both '/' and '*'

                # check for line comment opening //
                elif line[i] == "/" and i + 1 < len(line) and line[i + 1] == "/":
                    break  # rest of line is a comment, stop processing

                # normal character — keep it
                else:
                    current_line.append(line[i])
                    i += 1

        # end of source line:
        # only flush current_line to result if we are NOT inside a block comment
        # if in_block is True, the block spans to the next line — keep accumulating
        if not in_block and current_line:
            result.append("".join(current_line))
            current_line = []  # reset for next line

    return result


# ── Tests ─────────────────────────────────────────────────────────────────────
source1 = [
    "/*Test program */",
    "int main()",
    "{ ",
    "  // variable declaration ",
    "int a, b, c;",
    "/* This is a test",
    "   multiline  ",
    "   comment for ",
    "   testing */",
    "int b = 0;",
    "// int a = 0;",
    "}",
]
#print(removeComments(source1))
# ["int main()", "{ ", "  ", "int a, b, c;", "int b = 0;", "}"]

source2 = ["a/*comment", "line", "more_comment*/b"]
# print(removeComments(source2))
# ["ab"]

source3 = ["void func(int k) {", "// this is a comment", "return k;", "}"]
# print(removeComments(source3))
# ["void func(int k) {", "return k;", "}"]

source4 = ["int a = 1; /* start", "middle", "end */ int b = 2;"]
# print(removeComments(source4))
# ["int a = 1;  int b = 2;"]


# NOTE: I have to place this condition "line[i + 1] == "* or /" after line[i] == "/ or *"
def removeCommentsT(source: List[str]) -> List[str]:
    results = []
    current_line = []
    in_block = False

    for line in source:
        i = 0
        while i < len(line):

            # check if the line is inside a block comment
            if in_block:
                # we have come to the end of the line comment
                if line[i] == "*" and line[i + 1] == "/" and i + 1 < len(line):
                    in_block = False
                    i += 2  # skipping */
                else:
                    i += 1
            else:
                # check if block comments starts with / and *
                if line[i] == "/" and line[i + 1] == "*" and i + 1 < len(line):
                    in_block = True
                    i += 2  # skipping the /*
                    # check for comment opening that starts with //
                elif line[i] == "/" and line[i + 1] == "/" and i + 1 < len(line):
                    break
                else:
                    current_line.append(line[i])
                    i += 1

        # only append current line to results if we are not inside a block comment
        # if in_block is True, comments continues to the next line
        if not in_block and current_line:
            results.append("".join(current_line))
            current_line = []

    return results


print(removeCommentsT(source1))
