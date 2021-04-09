package com.github.asciborek.player;

import java.nio.file.Path;

public final class Track {

  private final String title;
  private final String album;
  private final String artist;
  private final int duration;
  private final String length;
  private final String fileName;
  private final Path filePath;

  public Track(String title, String album, String artist, int duration, Path filePath) {
    this.title = title;
    this.album = album;
    this.artist = artist;
    this.duration = duration;
    this.length = String.valueOf(duration);
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

  public int getDuration() {
    return duration;
  }

  public String getLength() {
    return length;
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
    private int duration;
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

    public TrackBuilder withDuration(int duration) {
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
