function myReplace(str = '', before = '', after = '') {
  if (str.includes(before)) {
    if (before.match(/^[a-z]/)) {
      const lowercase = `${after[0].toLowerCase()}${after.slice(1)}`;
      str = str.replace(before, lowercase);
    } else if (before.match(/^[A-Z]/)) {
      const uppercase = `${after[0].toUpperCase()}${after.slice(1)}`;
      str = str.replace(before, uppercase);
    }
  }
  return str;
}

myReplace('His name is Tom', 'Tom', 'john'); // "His name is John"
