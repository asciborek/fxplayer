package com.github.asciborek.metadata;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.google.common.io.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class TrackMetadataUpdater {

  private final Map<AudioFileExtension, AudioFileMetadataUpdater> updaters;

  TrackMetadataUpdater() {
    this(List.of(new Mp3AudioFileMetadataUpdater(), new Mp4AudioFileMetadataUpdater(),
        new WavAudioFileMetadataUpdater()));
  }

  TrackMetadataUpdater(Collection<AudioFileMetadataUpdater> updaters) {
    this.updaters = updaters.stream()
        .collect(toMap(AudioFileMetadataUpdater::supportedFileExtension, identity()));
  }

  void updateTrackMedata(Track track) {
    var extension = Files.getFileExtension(track.filePath().toString());
    var delegate = updaters.get(AudioFileExtension.valueOfIgnoreCase(extension));
    if (delegate == null) {
      throw new IllegalArgumentException(extension + " is not supported for update");
    }
    delegate.updateTrack(track);
  }


}
