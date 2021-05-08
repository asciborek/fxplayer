package com.github.asciborek.player;

import com.github.asciborek.util.DurationUtils;
import java.nio.file.Path;
import java.time.Duration;

public record Track (String title, String album, String artist, Duration duration, Path filePath) {

  public static TrackBuilder builder() {
    return TrackBuilder.builder();
  }

  public String length() {
    return DurationUtils.format(duration);
  }

  public String fileName() {
    return filePath.getFileName().toString();
  }

  public static final class TrackBuilder {

    private String title = "";
    private String album = "";
    private String artist = "";
    private Duration duration;
    private Path filePath;

    private TrackBuilder() {}

    public static TrackBuilder builder() {
      return new TrackBuilder();
    }

    public TrackBuilder withTitle(String title) {
      this.title = title;
      return this;
    }

    public TrackBuilder withAlbum(String album) {
      this.album = album;
      return this;
    }

    public TrackBuilder withArtist(String artist) {
      this.artist = artist;
      return this;
    }

    public TrackBuilder withDuration(Duration duration) {
      this.duration = duration;
      return this;
    }

    public TrackBuilder withFilePath(Path filePath) {
      this.filePath = filePath;
      return this;
    }

    public Track build() {
      return new Track(title, album, artist, duration, filePath);
    }
  }
}
