CREATE TABLE played_tracks_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    artist VARCHAR(100) NOT NULL,
    album VARCHAR (100) NOT NULL,
    track_title VARCHAR (100) NOT NULL,
    played_at LON NOT NULL
);