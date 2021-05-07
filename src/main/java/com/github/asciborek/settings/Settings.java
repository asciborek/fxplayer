package com.github.asciborek.settings;

import com.google.common.base.Strings;
import java.util.Optional;

final class Settings {

  private Double volumeLevel;

  private String addDirectoryChoicePath;

  private String addTrackChoicePath;

  public Settings() {}

  public Optional<Double> getVolumeLevel() {
    return Optional.ofNullable(volumeLevel);
  }

  public void setVolumeLevel(Double volumeLevel) {
    this.volumeLevel = volumeLevel;
  }

  public void setAddDirectoryChoicePath(String addDirectoryChoicePath) {
    this.addDirectoryChoicePath = addDirectoryChoicePath;
  }

  public Optional<String> getAddDirectoryChoicePath() {
    return Strings.isNullOrEmpty(addDirectoryChoicePath) ? Optional.empty() : Optional.of(
        addDirectoryChoicePath);
  }

  public void setAddTrackChoicePath(String addTrackChoicePath) {
    this.addTrackChoicePath = addTrackChoicePath;
  }

  public Optional<String> getAddTrackChoicePath() {
    return Strings.isNullOrEmpty(addTrackChoicePath) ? Optional.empty() : Optional.of(
        addTrackChoicePath);
  }
}
