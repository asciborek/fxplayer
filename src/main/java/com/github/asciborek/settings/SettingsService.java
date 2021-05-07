package com.github.asciborek.settings;

import com.github.asciborek.util.FileUtils;
import java.io.File;

public final class SettingsService {

  static final double MIN_VOLUME_LEVEL = 0;
  static final double MAX_VOLUME_LEVEL = 1;
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

  public void setAddTrackChoicePath(File selectTrackSuggestion) {
    settings.setAddTrackChoicePath(selectTrackSuggestion.getPath());
    settingsStorage.save(settings);
  }

  public File getAddTrackChoicePath() {
    return settings.getAddTrackChoicePath()
        .map(File::new)
        .orElseGet(this::getUserHome);
  }

  public void setAddDirectoryChoicePath(File selectDirectorySuggestion) {
    settings.setAddDirectoryChoicePath(selectDirectorySuggestion.getPath());
    settingsStorage.save(settings);
  }

  public File getAddDirectoryChoicePath() {
    return settings.getAddDirectoryChoicePath()
        .map(File::new)
        .orElseGet(this::getUserHome);
  }

  private File getUserHome() {
    return new File(FileUtils.getUserHome());
  }

}
