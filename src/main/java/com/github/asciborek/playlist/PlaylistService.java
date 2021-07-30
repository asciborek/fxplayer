package com.github.asciborek.playlist;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataProvider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public final class PlaylistService {

  private final ExecutorService executorService;
  private final TrackMetadataProvider trackMetadataProvider;
  private final PlaylistStorage playlistStorage;
  private final Collection<String> supportedAudioFilesExtensions;

  PlaylistService(ExecutorService executorService, TrackMetadataProvider trackMetadataProvider,
      PlaylistStorage playlistStorage, Collection<String> supportedAudioFilesExtensions) {
    this.executorService = executorService;
    this.trackMetadataProvider = trackMetadataProvider;
    this.playlistStorage = playlistStorage;
    this.supportedAudioFilesExtensions = supportedAudioFilesExtensions;
  }

  public Optional<Track> getTrack(File trackFile) {
    return trackMetadataProvider.getMetadata(trackFile);
  }

  public CompletableFuture<List<Track>> getDirectoryTracks(File directoryFile) {
    var directoryTracksLoader = new DirectoryTracksLoader(trackMetadataProvider,
        directoryFile.toPath(), supportedAudioFilesExtensions);
    return CompletableFuture.supplyAsync(directoryTracksLoader, executorService);
  }

  public void savePlaylist(File playlistFile, List<Track> playlist) {
    playlistStorage.savePlaylist(playlistFile.toPath(), playlist);
  }

  public CompletableFuture<List<Track>> loadPlaylistWithExistingFiles(File playlistFile) {
    return CompletableFuture.supplyAsync(() -> loadPlaylistWithExistingFiles(playlistFile.toPath()),
        executorService);
  }

  private List<Track> loadPlaylistWithExistingFiles(Path playlistPath) {
    return playlistStorage.loadPlaylist(playlistPath).stream()
        .filter(this::exists)
        .toList();
  }

  private boolean exists(Track track) {
    return Files.exists(track.filePath());
  }

}
