package com.github.asciborek.player;

import com.github.asciborek.util.FileUtils;
import com.google.common.base.Stopwatch;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DirectoryTracksProvider implements Supplier<List<Track>> {

  private static final List<String> SUPPORTED_EXTENSIONS = FileExtension.getSupportedExtensions();
  private static final Logger LOG = LoggerFactory.getLogger(DirectoryTracksProvider.class);

  private final Path directoryPath;

  public DirectoryTracksProvider(File directoryFile) {
    this.directoryPath = directoryFile.toPath();
  }

  @Override
  public List<Track> get() {
    try(Stream<Path> files = Files.walk(directoryPath)) {
      var stopWatch = Stopwatch.createStarted();
      var list =  files.filter(Files::isRegularFile)
          .filter(path -> FileUtils.hasSupportedExtension(path, SUPPORTED_EXTENSIONS))
          .map(MetadataUtils::getTrackMetaData)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .sorted(Comparator.comparing(Track::getFilePath))
          .collect(Collectors.toUnmodifiableList());
      var elapsedTime = stopWatch.stop().elapsed(TimeUnit.MILLISECONDS);
      LOG.info("got {} tracks from directory {}, elapsed time: {} ms", list.size(), directoryPath, elapsedTime);
      return list;
    } catch (Exception e) {
      LOG.error("fetch directory tracks error", e);
      return Collections.emptyList();
    }
  }

}
