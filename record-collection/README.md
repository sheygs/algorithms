##  Record Collection

```js
 const collection = {
  2548: {
    album: "Slippery When Wet",
    artist: "Bon Jovi",
    tracks: [
      "Let It Rock",
      "You Give Love a Bad Name"
    ]
  },
  2468: {
    album: "1999",
    artist: "Prince",
    tracks: [
      "1999",
      "Little Red Corvette"
    ]
  },
  1245: {
    artist: "Robert Palmer",
    tracks: [ ]
  },
  5439: {
    album: "ABBA Gold"
  }
};

```

```js
 updateRecords(5439, "artist", "ABBA"), artist should be "ABBA"
 updateRecords(5439, "tracks", "Take a Chance on Me"), tracks should have "Take a Chance on Me" as the last element.
 updateRecords(2548, "artist", ""), artist should not be set
 updateRecords(1245, "tracks", "Addicted to Love"), tracks should have "Addicted to Love" as the last element.
 updateRecords(2468, "tracks", "Free"), tracks should have "1999" as the first element.
 updateRecords(2548, "tracks", ""), tracks should not be set
 updateRecords(1245, "album", "Riptide"), album should be "Riptide"

```