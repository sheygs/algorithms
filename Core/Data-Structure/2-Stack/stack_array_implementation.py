class Stack:
    def __init__(self):
        self.array = []

    def peek(self):
        return self.array[len(self.array) - 1]

    def push(self, data):
        self.array.append(data)
        return

    # O(1) - removing the last element of a list is constant time
    def pop(self):
        if len(self.array) != 0:
            self.array.pop()
            return
        else:
            print("Stack Empty")
            return

    # O(n) - LIFO order requires walking the array back to front
    def print_stack(self):
        for i in range(len(self.array) - 1, -1, -1):
            print(self.array[i])
        return


my_stack = Stack()
my_stack.push("Andrei's")
my_stack.push("Courses")
my_stack.push("Are")
my_stack.push("Awesome")
my_stack.print_stack()

my_stack.pop()
my_stack.pop()
my_stack.print_stack()

print(my_stack.peek())

print(my_stack.__dict__)
