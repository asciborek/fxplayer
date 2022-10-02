package com.github.asciborek.settings;

import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.util.FileUtils;
import com.google.common.eventbus.Subscribe;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SettingsService {

  private static final Logger LOG = LoggerFactory.getLogger(SettingsService.class);
  static final double MIN_VOLUME_LEVEL = 0;
  static final double MAX_VOLUME_LEVEL = 1;
  private final SettingsStorage settingsStorage;
  private final Settings settings;

  SettingsService(SettingsStorage settingsStorage) {
    this.settingsStorage = settingsStorage;
    this.settings = settingsStorage.loadSettings()
        .orElseGet(Settings::new);
  }

  @Subscribe
  public void onCloseApplicationEvent(CloseApplicationEvent closeApplicationEvent) {
    LOG.info("save the application settings");
    settingsStorage.save(settings);
  }

  public void setVolume(double volumeLevel) {
    if (volumeLevel < MIN_VOLUME_LEVEL || volumeLevel > MAX_VOLUME_LEVEL) {
      throw new IllegalArgumentException();
    }
    settings.setVolumeLevel(volumeLevel);
  }

  public double getVolume() {
    return settings.getVolumeLevel().orElse(MAX_VOLUME_LEVEL);
  }

  public void setAddTrackFileChooserInitDirectory(File selectTrackSuggestion) {
    settings.setAddTrackFileChooserInitDirectory(selectTrackSuggestion.getPath());
  }

  public File getAddTrackFileChooserInitDirectory() {
    return settings.getAddTrackFileChooserInitDirectory()
        .map(File::new)
        .orElseGet(this::getUserHome);
  }

  public void setAddDirectoryDirectoryChooserInitDirectory(File selectDirectorySuggestion) {
    settings.setAddDirectoryDirectoryChooserInitDirectory(selectDirectorySuggestion.getPath());
  }

  public File getDirectoryDirectoryChooserInitDirectory() {
    return settings.getAddDirectoryDirectoryChooserInitDirectory()
        .map(File::new)
        .orElseGet(this::getUserHome);
  }

  public void setOpenFileFileChooserInitDirectory(File openTrackDirectory) {
    settings.setOpenFileFileChooserInitDirectory(openTrackDirectory.getPath());
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
