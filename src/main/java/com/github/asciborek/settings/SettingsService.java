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

  public void setAddTrackFileChooserInitDirectory(File selectTrackSuggestion) {
    settings.setAddTrackFileChooserInitDirectory(selectTrackSuggestion.getPath());
    settingsStorage.save(settings);
  }

  public File getAddTrackFileChooserInitDirectory() {
    return settings.getAddTrackFileChooserInitDirectory()
        .map(File::new)
        .orElseGet(this::getUserHome);
  }

  public void setAddDirectoryDirectoryChooserInitDirectory(File selectDirectorySuggestion) {
    settings.setAddDirectoryDirectoryChooserInitDirectory(selectDirectorySuggestion.getPath());
    settingsStorage.save(settings);
  }

  public File getDirectoryDirectoryChooserInitDirectory() {
    return settings.getAddDirectoryDirectoryChooserInitDirectory()
        .map(File::new)
        .orElseGet(this::getUserHome);
  }

  public void setOpenFileFileChooserInitDirectory(File openTrackDirectory) {
    settings.setOpenFileFileChooserInitDirectory(openTrackDirectory.getPath());
    settingsStorage.save(settings);
  }

  public File getOpenFileFileChooserInitDirectory() {
    return settings.getOpenFileFileChooserInitDirectory()
        .map(File::new)
        .orElseGet(this::getUserHome);
  }

  private File getUserHome() {
    return new File(FileUtils.getUserHome());
  }

}
