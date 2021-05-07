package com.github.asciborek.player;

import com.github.asciborek.util.FileUtils;
import com.github.asciborek.util.MetadataUtils;
import com.google.common.base.Stopwatch;
import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlaylistService {

  private static final Logger LOG = LoggerFactory.getLogger(PlaylistService.class);
  private final ExecutorService executorService;
  private final Iterable<String> supportedExtensions;

  public PlaylistService(ExecutorService executorService, Iterable<String> supportedExtensions) {
    this.executorService = executorService;
    this.supportedExtensions = supportedExtensions;
  }

  Optional<Track> getTrack(File trackFile) {
    return MetadataUtils.getTrackMetaData(trackFile);
  }

  CompletableFuture<List<Track>> getDirectoryTracks(File directoryFile) {
    return CompletableFuture.supplyAsync(() -> loadTracks(directoryFile), executorService);
  }

  private List<Track> loadTracks(File directoryFile) {
    var stopWatch = Stopwatch.createStarted();
    try (Stream<Path> pathStream = FileUtils.getDirectoryFilesWithSupportedExtensions(directoryFile.toPath(), supportedExtensions)) {
      var resultList =  pathStream.map(MetadataUtils::getTrackMetaData)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .sorted(Comparator.comparing(Track::getFilePath))
          .collect(Collectors.toUnmodifiableList());
      var elapsed = stopWatch.stop().elapsed(TimeUnit.MICROSECONDS);
      LOG.info("loaded {} tracks from directory {}, elapsed time: {} ms", resultList.size(), directoryFile, elapsed);
      return resultList;
    } catch (Exception e) {
      return List.of();
    }
  }

}
