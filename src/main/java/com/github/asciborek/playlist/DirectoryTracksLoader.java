package com.github.asciborek.playlist;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataProvider;
import com.github.asciborek.util.FileUtils;
import com.google.common.base.Stopwatch;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DirectoryTracksLoader implements Supplier<List<Track>> {

  private static final Logger LOG = LoggerFactory.getLogger(DirectoryTracksLoader.class);

  private final TrackMetadataProvider trackMetadataProvider;
  private final Path directoryPath;
  private final Collection<String> supportedAudioFilesExtensions;

  public DirectoryTracksLoader(TrackMetadataProvider trackMetadataProvider,
      Path directoryPath, Collection<String> supportedAudioFilesExtensions) {
    this.trackMetadataProvider = trackMetadataProvider;
    this.directoryPath = directoryPath;
    this.supportedAudioFilesExtensions = supportedAudioFilesExtensions;
  }

  @Override
  public List<Track> get() {
    var stopWatch = Stopwatch.createStarted();
    try (Stream<Path> pathStream = FileUtils.getDirectoryFilesWithSupportedExtensions(directoryPath,
        supportedAudioFilesExtensions)) {
      var resultList = pathStream.map(trackMetadataProvider::getMetadata)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .sorted(Comparator.comparing(Track::filePath))
          .toList();
      var elapsed = stopWatch.stop().elapsed(TimeUnit.MICROSECONDS);
      LOG.info("loaded {} tracks from directory {}, elapsed time: {} ms", resultList.size(), directoryPath, elapsed);
      return resultList;
    } catch (Exception e) {
      LOG.error("directory tracks loading error", e);
      return List.of();
    }
  }
}
