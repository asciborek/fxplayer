--should contain max single row with the id equal 1;

CREATE TABLE last_fm_settings (
    id INTEGER PRIMARY KEY,
    signed_int INTEGER NOT NULL DEFAULT 0,
    username VARCHAR (100)
);