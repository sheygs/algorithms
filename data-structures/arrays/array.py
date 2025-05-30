"""
 Array Implementation
"""


class MyArray:
    def __init__(self):
        self.length = 0
        self.store = {}

    # Time complexity: O(1)
    def get(self):
        print('self: ', self.store.values())
        return str(self.__dict__)

    # Time complexity: O(1)
    def append(self, item):
        self.length += 1
        self.store[self.length - 1] = item

    # Time complexity: O(1)
    def pop(self):
        if self.length == 0 or not self.store:
            print('array already empty!')
            return
        removed = self.store[self.length - 1]
        del self.store[self.length - 1]
        self.length -= 1
        return removed

    # Time complexity: O(n)
    def insert(self, item, index: int) -> None:
        self.length += 1

        if index < 0 or index > self.length - 1:
            raise ValueError('Invalid index range')

        for i in range(self.length - 1, index, -1):
            self.store[i] = self.store[i - 1]

        self.store[index] = item

    # Time complexity: O(n)
    def delete(self, index):
        if index < 0 or index > self.length - 1:
            raise ValueError('Invalid index range')

        for i in range(index, self.length - 1):
            self.store[i] = self.store[i + 1]

        del self.store[index]
        self.length -= 1


if __name__ == "__main__":
    arr = MyArray()
    arr.append('one')
    arr.append('two')
    arr.append('four')
    arr.append('five')
    # arr.delete(3)
    print(f'array: {arr.get()}')
