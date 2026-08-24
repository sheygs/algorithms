class MyArray:
    def __init__(self):
        # Time: O(1) - Simple assignment operations
        # Space: O(1) - Only storing two primitive values
        self.length = 0  # Initialize length counter - O(1) time, O(1) space
        self.data = (
            {}
        )  # Initialize empty dictionary - O(1) time, O(1) space for empty dict

    def __str__(self):
        """Return a clean string representation of the array elements"""
        # Overall Time: O(n) where n is the length of array
        # Overall Space: O(n) for creating the string representation

        if self.length == 0:  # O(1) - Simple comparison
            return "[]"  # O(1) - Return constant string

        # O(n) time - iterating through all elements
        # O(n) space - creating new list with n string elements
        elements = [str(self.data[i]) for i in range(self.length)]

        # O(n) time - join operation processes all elements
        # O(n) space - creates new string of total length proportional to content
        return "[" + ", ".join(elements) + "]"

    def __repr__(self):
        """Return a detailed representation for debugging"""
        # Time: O(n) - list(self.data.values()) iterates through all elements
        # Space: O(n) - creates new list containing all values
        return f"MyArray(length={self.length}, data={list(self.data.values())})"

    def _validate_index(self, index, allow_end=False):
        """Validate if index is within bounds"""
        # Time: O(1) - Only arithmetic and comparison operations
        # Space: O(1) - No additional data structures created

        max_index = (
            self.length if allow_end else self.length - 1
        )  # O(1) - conditional assignment
        if index < 0 or index > max_index:  # O(1) - comparison operations
            # O(1) - string formatting and exception creation
            raise IndexError(
                f"Index {index} out of range for array of length {self.length}"
            )

    def get(self, index):
        """Get element at specified index - O(1)"""
        # Overall Time: O(1) - validation is O(1) + dict lookup is O(1)
        # Overall Space: O(1) - no additional space used

        self._validate_index(index)  # O(1) time, O(1) space - index validation
        return self.data[index]  # O(1) time - dictionary lookup by key, O(1) space

    def push(self, item):
        """Add item to end of array - O(1)"""
        # Overall Time: O(1) - both operations are constant time
        # Overall Space: O(1) - only storing one additional element

        self.data[self.length] = item  # O(1) - dictionary insertion at specific key
        self.length += 1  # O(1) - increment operation

    def pop(self):
        """Remove and return last element - O(1)"""
        # Overall Time: O(1) - all operations are constant time
        # Overall Space: O(1) - temporarily storing one element

        if self.length == 0:  # O(1) - comparison
            raise IndexError("Cannot pop from empty array")  # O(1) - exception creation

        last_item = self.data[self.length - 1]  # O(1) - dict access by key
        del self.data[self.length - 1]  # O(1) - dict deletion by key
        self.length -= 1  # O(1) - decrement operation
        return last_item  # O(1) - return stored value

    def insert(self, index, item):
        """Insert item at specified index - O(n)"""
        # Overall Time: O(n) - due to shifting elements
        # Overall Space: O(1) - only temporary variables, no additional data structures

        # Allow insertion at the end (index == length)
        self._validate_index(index, allow_end=True)  # O(1) - validation

        # Shift elements to the right - THIS IS THE O(n) OPERATION
        # Time: O(n) - in worst case (insert at index 0), we shift all n elements
        # Space: O(1) - only using loop variable i, no additional storage
        for i in range(self.length, index, -1):  # O(n) iterations in worst case
            self.data[i] = self.data[
                i - 1
            ]  # O(1) per iteration - dict access + assignment

        # Insert the new item
        self.data[index] = item  # O(1) - dictionary assignment
        self.length += 1  # O(1) - increment

    def delete(self, index):
        """Delete element at specified index - O(n)"""
        # Overall Time: O(n) - due to shifting elements left
        # Overall Space: O(1) - only storing deleted item temporarily

        self._validate_index(index)  # O(1) - validation

        deleted_item = self.data[index]  # O(1) - dict access and storage

        # Shift elements to the left - THIS IS THE O(n) OPERATION
        # Time: O(n) - in worst case (delete at index 0), we shift n-1 elements
        # Space: O(1) - only using loop variable i
        for i in range(index, self.length - 1):  # O(n) iterations in worst case
            self.data[i] = self.data[i + 1]  # O(1) per iteration - dict operations

        # Remove the last element and decrease length
        del self.data[self.length - 1]  # O(1) - dict deletion
        self.length -= 1  # O(1) - decrement

        return deleted_item  # O(1) - return stored value

    def size(self):
        """Return the length of the array"""
        # Time: O(1) - simple attribute access
        # Space: O(1) - no additional space used
        return self.length

    def is_empty(self):
        """Check if array is empty"""
        # Time: O(1) - simple comparison
        # Space: O(1) - no additional space used
        return self.length == 0

    def clear(self):
        """Remove all elements from the array"""
        # Time: O(1) - dict.clear() is O(1) in Python, assignment is O(1)
        # Space: O(1) - no additional space, actually frees O(n) space
        self.data.clear()  # O(1) - clears dictionary
        self.length = 0  # O(1) - reset length

    def to_list(self):
        """Convert to Python list"""
        # Time: O(n) - must iterate through all n elements
        # Space: O(n) - creates new list containing all n elements
        return [self.data[i] for i in range(self.length)]


# COMPLEXITY ANALYSIS SUMMARY:
"""
SPACE COMPLEXITY:
- Overall space complexity of the data structure: O(n) where n is number of elements
- The dictionary self.data stores n key-value pairs
- Each operation uses O(1) additional space except __str__, __repr__, and to_list() which use O(n)

TIME COMPLEXITY BY OPERATION:
- get(index): O(1) - direct dictionary access
- push(item): O(1) - append to end
- pop(): O(1) - remove from end
- insert(index, item): O(n) - may need to shift up to n elements
- delete(index): O(n) - may need to shift up to n-1 elements
- size(): O(1) - return stored length
- is_empty(): O(1) - simple comparison
- clear(): O(1) - dictionary clear operation
- __str__(): O(n) - must process all elements
- to_list(): O(n) - must copy all elements

WHY DICTIONARY OPERATIONS ARE O(1):
- Python dictionaries use hash tables
- Hash table operations (access, insert, delete) are O(1) average case
- Our integer keys (0, 1, 2, ...) hash perfectly with no collisions

WHY INSERT/DELETE ARE O(n):
- Array elements must be contiguous (no gaps)
- Inserting in middle requires shifting all elements after it
- Deleting from middle requires shifting all elements after it
- Only end operations (push/pop) avoid shifting, hence O(1)
"""


# Example usage demonstrating complexity:
if __name__ == "__main__":
    arr = MyArray()

    # O(1) operations
    arr.push(1)  # O(1) - add to end
    arr.push(2)  # O(1) - add to end
    arr.push(3)  # O(1) - add to end
    print(arr.get(1))  # O(1) - direct access

    # O(n) operations
    arr.insert(0, 0)  # O(n) - insert at beginning, shifts all elements
    arr.delete(2)  # O(n) - delete from middle, shifts remaining elements

    # O(1) operations
    arr.pop()  # O(1) - remove from end
    print(arr.size())  # O(1) - return stored length
