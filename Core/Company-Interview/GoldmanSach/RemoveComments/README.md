# 722. Remove Comments

## Difficulty

Medium

## Problem Statement

Given a C++ program, remove comments from it. The program source is an array of strings
`source` where `source[i]` is the `i`-th line of the source code. This represents the result
of splitting the original source code string by the newline character `'\n'`.

In C++, there are two types of comments, both of which start with a forward slash (`/`):

- **Line comments** begin with `//` and extend to the end of the line.
- **Block comments** begin with `/*` and end with `*/`. A block comment can span multiple lines.

The first comment taking effect when reading the source code from left to right is the one
that starts the comment — any comment markers inside a comment are ignored.

Do not include any empty lines in your answer. The final line of the result will not have
trailing spaces.

---

## Examples

**Example 1:**

```text
Input: source = ["/*Test program */", "int main()", "{ ", "  // variable declaration ",
                 "int a, b, c;", "/* This is a test", "   multiline  ", "   comment for ",
                 "   testing */","int b = 0;", "// int a = 0;", "}"]

Output: ["int main()","{ ","  ","int a, b, c;","int b = 0;","}"]
```

**Example 2:**

```text
Input: source = ["a/*comment", "line", "more_comment*/b"]
Output: ["ab"]
Explanation: The original source string is "a/*comment\nline\nmore_comment*/b", which
             becomes "ab" after removing the block comment.
```

---

## Constraints

- `1 <= source.length <= 100`
- `0 <= source[i].length <= 80`
- `source[i]` consists of printable ASCII characters.
- Every open block comment is eventually closed.
- There are no single-quote or double-quote in the input.

---

## Topics

`Array` `String`
