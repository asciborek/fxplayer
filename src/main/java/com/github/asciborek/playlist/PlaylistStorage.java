package com.github.asciborek.playlist;

import java.nio.file.Path;
import java.util.List;

interface PlaylistStorage {

  void savePlaylist(Path playlistFilePath, List<Track> playlist);

  List<Track> loadPlaylist(Path playlistFilePath);

}
