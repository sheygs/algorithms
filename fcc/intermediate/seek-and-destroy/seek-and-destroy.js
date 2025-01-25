// Seek And Destroy
const destroyer = (arr, ...list) => arr.filter((elt) => !list.includes(elt));

destroyer([1, 2, 3, 5, 1, 2, 3], 2, 3); // returns [1,5,1]
