package com.github.asciborek.settings;

public final class SettingsService {

  private static final double MIN_VOLUME_LEVEL = 0;
  private static final double MAX_VOLUME_LEVEL = 1;
  private final SettingsStorage settingsStorage;
  private final Settings settings;

  SettingsService(SettingsStorage settingsStorage) {
    this.settingsStorage = settingsStorage;
    this.settings = settingsStorage.loadSettings()
        .orElseGet(Settings::new);
  }

  public void setVolume(double volumeLevel) {
    if (volumeLevel < MIN_VOLUME_LEVEL || volumeLevel > MAX_VOLUME_LEVEL) {
      throw new IllegalArgumentException();
    }
    settings.setVolumeLevel(volumeLevel);
    settingsStorage.save(settings);
  }

  public double getVolume() {
    return settings.getVolumeLevel().orElse(MAX_VOLUME_LEVEL);
  }
}
