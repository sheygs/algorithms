"""
 Array Implementation
"""


class Array:
    def __init__(self):
        self.name = 'array'
        self.length = 0
        self.store = {}

    # Time complexity: o(1)
    def get(self):
        return self.__dict__

    # Time complexity: o(1)
    def append(self, item):
        self.length += 1
        self.store[self.length - 1] = item

    # Time complexity: o(1)
    def pop(self):
        if self.length == 0 or not self.store:
            print('array already empty!')
            return
        removed = self.store[self.length - 1]
        del self.store[self.length - 1]
        self.length -= 1
        return removed


arr = Array()
arr.append('1')
arr.append('2')
arr.pop()
arr.pop()
arr.pop()
print(f'array: {arr.get()}')
