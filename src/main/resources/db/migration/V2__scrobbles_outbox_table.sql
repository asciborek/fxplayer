CREATE TABLE scrobbles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    artist VARCHAR(100) NOT NULL,
    album VARCHAR (100) NOT NULL,
    track VARCHAR (100) NOT NULL,
    timestamp INTEGER NOT NULL
);

CREATE INDEX timestamp_idx ON scrobbles(timestamp);