class CustomArray:
    def __init__(self):
        self.length = 0
        self.data = {}

    # Time complexity: O(1)
    def get(self):
        print("self: ", self.data.values())
        return str(self.__dict__)

    # Time complexity: O(1)
    def append(self, item):
        self.length += 1
        self.data[self.length - 1] = item

    # Time complexity: O(1)
    def pop(self):
        if self.length == 0 or not self.data:
            print("array already empty!")
            return
        removed = self.data[self.length - 1]
        del self.data[self.length - 1]
        self.length -= 1
        return removed

    def __validate_index(self, index):
        if index < 0 or index > self.length - 1:
            raise IndexError(f"Index out of range for array of length {self.length}")

    # Time complexity: O(n)
    def insert(self, item, index: int) -> None:
        self.length += 1

        self.__validate_index(index)

        for i in range(self.length - 1, index, -1):
            self.data[i] = self.data[i - 1]

        self.data[index] = item

    # Time complexity: O(n)
    def delete(self, index):
        self.__validate_index(index)

        for i in range(index, self.length - 1):
            self.data[i] = self.data[i + 1]

        del self.data[index]
        self.length -= 1

    # Time complexity: O(1)
    def clear(self):
        self.data.clear()
        self.length = 0

    # Time complexity: O(1)
    def is_empty(self):
        return self.length == 0

    # Time complexity: O(1)
    def size(self):
        return self.length


if __name__ == "__main__":
    arr = CustomArray()
    arr.append("one")
    arr.append("two")
    arr.append("four")
    arr.append("five")
    arr.insert("six", 1)
    # arr.delete(3)
    # arr.clear()
    print(f"Is array empty: {arr.is_empty()}")
    print(f"Array size: {arr.size()}")
    print(f"array: {arr.get()}")
