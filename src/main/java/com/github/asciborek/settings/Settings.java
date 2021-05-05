package com.github.asciborek.settings;

import java.util.Optional;

final class Settings {

  private Double volumeLevel;

  public Settings() {}

  public Settings(double volumeLevel) {
    this.volumeLevel = volumeLevel;
  }

  public Optional<Double> getVolumeLevel() {
    return Optional.ofNullable(volumeLevel);
  }

  public void setVolumeLevel(Double volumeLevel) {
    this.volumeLevel = volumeLevel;
  }
}
