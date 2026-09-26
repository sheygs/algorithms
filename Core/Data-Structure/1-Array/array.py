class MyArray:
    def __init__(self):
        self.length = 0
        self.data = {}

    def __str__(self):
        if self.length == 0:
            return "[]"
        elements = [str(self.data[i]) for i in range(self.length)]
        return "[" + ", ".join(elements) + "]"

    def __repr__(self):
        return f"MyArray(length={self.length}, data={list(self.data.values())})"

    def _validate_index(self, index, allow_end=False):
        max_index = self.length if allow_end else self.length - 1
        if index < 0 or index > max_index:
            raise IndexError(
                f"Index {index} out of range for array of length {self.length}"
            )

    def get(self, index):
        """O(1)"""
        self._validate_index(index)
        return self.data[index]

    def push(self, item):
        """O(1)"""
        self.data[self.length] = item
        self.length += 1

    def pop(self):
        """O(1)"""
        if self.length == 0:
            raise IndexError("Cannot pop from empty array")

        last_item = self.data[self.length - 1]
        del self.data[self.length - 1]
        self.length -= 1
        return last_item

    def insert(self, index, item):
        """O(n) - shifts every element after index one slot right"""
        self._validate_index(index, allow_end=True)

        for i in range(self.length, index, -1):
            self.data[i] = self.data[i - 1]

        self.data[index] = item
        self.length += 1

    def delete(self, index):
        """O(n) - shifts every element after index one slot left"""
        self._validate_index(index)

        deleted_item = self.data[index]

        for i in range(index, self.length - 1):
            self.data[i] = self.data[i + 1]

        del self.data[self.length - 1]
        self.length -= 1

        return deleted_item

    def size(self):
        """O(1)"""
        return self.length

    def is_empty(self):
        """O(1)"""
        return self.length == 0

    def clear(self):
        """O(1)"""
        self.data.clear()
        self.length = 0

    def to_list(self):
        """O(n)"""
        return [self.data[i] for i in range(self.length)]


if __name__ == "__main__":
    arr = MyArray()

    arr.push(1)
    arr.push(2)
    arr.push(3)
    print(arr.get(1))

    arr.insert(0, 0)
    arr.delete(2)

    arr.pop()
    print(arr.size())
