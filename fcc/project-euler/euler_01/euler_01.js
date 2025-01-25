function isMultiple(num) {
  if (!Number.isInteger(num)) return;
  return num % 3 === 0 || num % 5 === 0;
}

function rangeSequenceBelow(max) {
  const range = Array(max)
    .fill()
    .map((_, i) => i);
  return range;
}

function getAllMultiples(max = 5) {
  const multiples = rangeSequenceBelow(max).map((value) => {
    return isMultiple(value) ? value : null;
  });

  return multiples.filter(Boolean);
}

function getSum(array = []) {
  const total = array.reduce((accumulator, next) => accumulator + next, 0);
  return total;
}

function multiplesOf3and5(number) {
  const sum = getSum(getAllMultiples(number));
  return sum;
}

multiplesOf3and5(1000);
