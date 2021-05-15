package com.github.asciborek.playlist;

import com.github.asciborek.util.MetadataUtils;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public final class PlaylistService {

  private final ExecutorService executorService;
  private final Collection<String> supportedAudioFilesExtensions;

  PlaylistService(ExecutorService executorService,
      Collection<String> supportedAudioFilesExtensions) {
    this.executorService = executorService;
    this.supportedAudioFilesExtensions = supportedAudioFilesExtensions;
  }

  public Optional<Track> getTrack(File trackFile) {
    return MetadataUtils.getTrackMetaData(trackFile);
  }

  public CompletableFuture<List<Track>> getDirectoryTracks(File directoryFile) {
    var directoryTracksLoader = new DirectoryTracksLoader(directoryFile.toPath(),
        supportedAudioFilesExtensions);
    return CompletableFuture.supplyAsync(directoryTracksLoader, executorService);
  }

}
