# Topics: Tree, DFS, Binary Tree

from typing import Optional


class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right


def isBalanced():
    pass


def isBalancedOptimal(root: Optional[TreeNode]) -> bool:

    def dfs(root):
        if not root:
            return [True, 0]

        left, right = dfs(root.left), dfs(root.right)
        balanced = left[0] and right[0] and abs(left[1] - right[1]) <= 1

        return [balanced, 1 + max(left[1], right[1])]

    return dfs(root)[0]


# added
def build_tree(vals):
    if not vals:
        return None
    from collections import deque

    root = TreeNode(vals[0])
    queue = deque([root])
    i = 1
    while queue and i < len(vals):
        node = queue.popleft()
        if i < len(vals) and vals[i] is not None:
            node.left = TreeNode(vals[i])
            queue.append(node.left)
        i += 1
        if i < len(vals) and vals[i] is not None:
            node.right = TreeNode(vals[i])
            queue.append(node.right)
        i += 1
    return root


print(isBalancedOptimal(build_tree([3, 9, 20, None, None, 15, 7])))
# print(isBalancedOptimal(build_tree([1, 2, 2, 3, 3, None, None, 4, 4])))
