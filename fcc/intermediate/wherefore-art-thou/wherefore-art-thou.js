const whatIsInAName = (collection = [], source = {}) => {
  const keys = Object.keys(source);
  return collection.filter((obj) => {
    return keys.every((key) => {
      return obj.hasOwnProperty(key) && source[key] === obj[key];
    });
  });
};

whatIsInAName(
  [{ apple: 1, bat: 2 }, { apple: 1 }, { apple: 1, bat: 2, cookie: 2 }, { bat: 2 }],
  { apple: 1, bat: 2 },
);
