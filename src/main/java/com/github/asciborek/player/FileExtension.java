package com.github.asciborek.player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FileExtension {
  MP3(".mp3");
  private static final List<String> SUPPORTED_EXTENSIONS = supportedExtensions();
  private final String extension;

  FileExtension(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

  private static List<String> supportedExtensions() {
    return Stream.of(values())
        .map(FileExtension::getExtension)
        .collect(Collectors.toUnmodifiableList());
  }

  public static List<String> getSupportedExtensions() {
    return SUPPORTED_EXTENSIONS;
  }
}
