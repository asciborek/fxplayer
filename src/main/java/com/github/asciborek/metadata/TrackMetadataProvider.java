package com.github.asciborek.metadata;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.google.common.io.Files;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TrackMetadataProvider {

  private final Map<AudioFileExtension, AudioFileMetadataProvider> providers;

  public TrackMetadataProvider() {
    this(List.of(new Mp3AudioFileMetadataProvider(), new Mp4AudioFileMetadataProvider(),
        new WavAudioFileMetadataProvider()));
  }

  TrackMetadataProvider(Collection<AudioFileMetadataProvider> providers) {
    this.providers = providers.stream()
        .collect(toMap(AudioFileMetadataProvider::supportedFileExtension, identity()));
  }

  public Optional<Track> getMetadata(Path path) {
    return getMetadata(path.toFile());
  }

  public Optional<Track> getMetadata(File file) {
    String extension = Files.getFileExtension(file.toString());
    var delegate = providers.get(AudioFileExtension.valueOfIgnoreCase(extension));
    if (delegate == null) {
      throw new IllegalArgumentException(STR."Unsupported file extension \{extension}");
    }
    return delegate.getTrackMetadata(file);
  }

}
