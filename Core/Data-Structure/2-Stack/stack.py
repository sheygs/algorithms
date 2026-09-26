class CustomStack:
    def __init__(self):
        self.array = []

    # Time Complexity: O(1)
    def is_empty(self):
        return len(self.array) == 0

    # Time Complexity: O(1)
    def clear(self):
        return self.array.clear()

    # Time Complexity: O(1)
    def push(self, item):
        if not isinstance(item, (str, int)):
            raise ValueError("value cannot be null")
        self.array.append(item)
        return

    # Time Complexity: O(1)
    def pop(self):
        if self.is_empty():
            raise ValueError("stack is empty")
        return self.array.pop()

    # Time Complexity: O(1)
    def peek(self):
        if self.is_empty():
            raise ValueError("stack is empty")
        last_index = len(self.array) - 1
        return self.array[last_index]

    # reversed to reflect LIFO order (most recently pushed first)
    # Time Complexity: O(n)
    def log(self):
        return [self.array[i] for i in range(len(self.array) - 1, -1, -1)]


if __name__ == "__main__":
    my_stack = CustomStack()
    my_stack.push("2")
    my_stack.push("3")
    my_stack.push("1")
    print("peeked: ", my_stack.peek())
    print(f"elements left: {my_stack.log()}")
