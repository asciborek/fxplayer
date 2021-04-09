package com.github.asciborek.player;

import java.nio.file.Path;
import java.time.Duration;

public final class Track {

  private final String title;
  private final String album;
  private final String artist;
  private final Duration duration;
  private final String fileName;
  private final Path filePath;

  public Track(String title, String album, String artist, Duration duration, Path filePath) {
    this.title = title;
    this.album = album;
    this.artist = artist;
    this.duration = duration;
    this.filePath = filePath;
    this.fileName = filePath.getFileName().toString();
  }

  public static TrackBuilder builder() {
    return TrackBuilder.builder();
  }

  public String getTitle() {
    return title;
  }

  public String getAlbum() {
    return album;
  }

  public String getArtist() {
    return artist;
  }

  public Duration getDuration() {
    return duration;
  }

  public String getLength() {
    return String.format("%02d:%02d", duration.toMinutes(), duration.toSeconds());
  }

  public String getFileName() {
    return fileName;
  }

  public Path getFilePath() {
    return filePath;
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
