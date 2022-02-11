--should contain max single row with the id equal 1;

CREATE TABLE settings (
    id INTEGER PRIMARY KEY,
    volume_level DECIMAL,
    add_directory_directory_chooser_init_directory VARCHAR (100),
    add_track_file_chooser_init_directory VARCHAR (100),
    open_file_file_chooser_init_directory VARCHAR (100),
    CONSTRAINT volume_level_ch CHECK (volume_level >= 0.0 AND volume_level <= 1.0)
);