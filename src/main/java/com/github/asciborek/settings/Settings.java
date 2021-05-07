package com.github.asciborek.settings;

import com.google.common.base.Strings;
import java.util.Optional;

final class Settings {

  private Double volumeLevel;

  private String addDirectoryDirectoryChooserInitDirectory;

  private String addTrackFileChooserInitDirectory;

  private String openFileFileChooserInitDirectory;

  public Settings() {}

  public Optional<Double> getVolumeLevel() {
    return Optional.ofNullable(volumeLevel);
  }

  public void setVolumeLevel(Double volumeLevel) {
    this.volumeLevel = volumeLevel;
  }

  public void setAddDirectoryDirectoryChooserInitDirectory(String addDirectoryDirectoryChooserInitDirectory) {
    this.addDirectoryDirectoryChooserInitDirectory = addDirectoryDirectoryChooserInitDirectory;
  }

  public Optional<String> getAddDirectoryDirectoryChooserInitDirectory() {
    return Strings.isNullOrEmpty(addDirectoryDirectoryChooserInitDirectory) ? Optional.empty() : Optional.of(
        addDirectoryDirectoryChooserInitDirectory);
  }

  public void setAddTrackFileChooserInitDirectory(String addTrackFileChooserInitDirectory) {
    this.addTrackFileChooserInitDirectory = addTrackFileChooserInitDirectory;
  }

  public Optional<String> getAddTrackFileChooserInitDirectory() {
    return Strings.isNullOrEmpty(addTrackFileChooserInitDirectory) ? Optional.empty() : Optional.of(
        addTrackFileChooserInitDirectory);
  }

  public void setOpenFileFileChooserInitDirectory(String openFileFileChooserInitDirectory) {
    this.openFileFileChooserInitDirectory = openFileFileChooserInitDirectory;
  }

  public Optional<String> getOpenFileFileChooserInitDirectory() {
    return Strings.isNullOrEmpty(openFileFileChooserInitDirectory) ? Optional.empty() : Optional.of(
        openFileFileChooserInitDirectory);
  }
}
