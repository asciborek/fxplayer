package com.github.asciborek.metadata;

public enum AudioFileExtension {
  MP3("mp3"), MP4("mp4"), WAV("wav");

  private final String extension;

  AudioFileExtension(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

  public static AudioFileExtension valueOfIgnoreCase(String extension) {
    return valueOf(extension.toUpperCase());
  }
}
