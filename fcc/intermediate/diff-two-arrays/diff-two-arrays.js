// difference of Two Arrays

const diffArray = (arr1 = [], arr2 = []) => {
  // concat both arrays
  // apply set to get non-duplicate numbers {returns object}
  // convert from object to array
  // return the element not present in EITHER of the arrays but NOT both
  const uniqueArr = new Set(arr1.concat(arr2));

  return Array.from(uniqueArr).filter(
    (value) => !arr1.includes(value) || !arr2.includes(value),
  );
};
