package com.github.asciborek.settings;

import com.github.asciborek.util.FileUtils;
import java.io.File;

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

  public void setAddTrackInitPath(File selectTrackSuggestion) {
    settings.setAddTrackInitPath(selectTrackSuggestion.getPath());
    settingsStorage.save(settings);
  }

  public File getAddTrackInitPath() {
    return settings.getAddTrackInitPath()
        .map(File::new)
        .orElseGet(this::getUserHome);
  }

  public void setAddDirectoryInitPath(File selectDirectorySuggestion) {
    settings.setAddDirectoryInitPath(selectDirectorySuggestion.getPath());
    settingsStorage.save(settings);
  }

  public File getAddDirectoryInitPath() {
    return settings.getAddDirectoryInitPath()
        .map(File::new)
        .orElseGet(this::getUserHome);
  }

  private File getUserHome() {
    return new File(FileUtils.getUserHome());
  }

}
