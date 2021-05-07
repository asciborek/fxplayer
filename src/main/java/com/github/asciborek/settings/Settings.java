package com.github.asciborek.settings;

import com.google.common.base.Strings;
import java.util.Optional;

final class Settings {

  private Double volumeLevel;

  private String addDirectoryInitPath;

  private String addTrackInitPath;

  public Settings() {}

  public Optional<Double> getVolumeLevel() {
    return Optional.ofNullable(volumeLevel);
  }

  public void setVolumeLevel(Double volumeLevel) {
    this.volumeLevel = volumeLevel;
  }

  public void setAddDirectoryInitPath(String addDirectoryInitPath) {
    this.addDirectoryInitPath = addDirectoryInitPath;
  }

  public Optional<String> getAddDirectoryInitPath() {
    return Strings.isNullOrEmpty(addDirectoryInitPath) ? Optional.empty() : Optional.of(
        addDirectoryInitPath);
  }

  public void setAddTrackInitPath(String addTrackInitPath) {
    this.addTrackInitPath = addTrackInitPath;
  }

  public Optional<String> getAddTrackInitPath() {
    return Strings.isNullOrEmpty(addTrackInitPath) ? Optional.empty() : Optional.of(
        addTrackInitPath);
  }
}
