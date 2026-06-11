package com.github.asciborek.settings;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.TestUtils;
import com.github.asciborek.util.FileUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class SettingsServiceTest {

  private final Path settingsFile = getTempFile();
  private final SettingsStorage fileSettingsStorage = settingsStorage(settingsFile);

  @Test
  @DisplayName("load the default settings if the settings file doesn't exist")
  void loadDefaultSettingsIfSettingsFileDoesNotExist() {
    var settingsService = new SettingsService(fileSettingsStorage);

    //default volume value is max
    assertThat(SettingsService.MAX_VOLUME_LEVEL).isEqualTo(settingsService.getVolume());
    //default directory should be home directory
    assertThat(settingsService.getAddTrackFileChooserInitDirectory()).isEqualTo(new File(FileUtils.getUserHome()));
    assertThat(settingsService.getDirectoryDirectoryChooserInitDirectory()).isEqualTo(new File(FileUtils.getUserHome()));
    assertThat(settingsService.getOpenFileFileChooserInitDirectory()).isEqualTo(new File(FileUtils.getUserHome()));
    //default LastFm settings should have lastFmEnabled=true and offlineModeEnabled=false
    var lastFmSettings = settingsService.getLastFmSettings();
    assertThat(lastFmSettings.scrobblingEnabled()).isTrue();
    assertThat(lastFmSettings.offlineModeEnabled()).isFalse();
  }

  @Test
  @DisplayName("save and read settings in and from file")
  void saveAndReadSettingsInFromFile() {
    var expectedVolumeValue = 0.3;
    var expectedAddTrackFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Affinity");
    var expectedAddDirectoryDirectoryChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Virus");
    var expectedOpenFileFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Vector");
    var expectedLastFmSettings = new LastFmSettings(true, true);

    var settingsService = new SettingsService(fileSettingsStorage);
    settingsService.setVolume(expectedVolumeValue);
    settingsService.setAddTrackFileChooserInitDirectory(expectedAddTrackFileChooserDirectory);
    settingsService.setAddDirectoryDirectoryChooserInitDirectory(expectedAddDirectoryDirectoryChooserDirectory);
    settingsService.setOpenFileFileChooserInitDirectory(expectedOpenFileFileChooserDirectory);
    settingsService.setLastFmSettings(expectedLastFmSettings);
    settingsService.onCloseApplicationEvent(new CloseApplicationEvent());

    var newSettingsService = new SettingsService(fileSettingsStorage);
    assertThat(newSettingsService.getVolume()).isEqualTo(expectedVolumeValue);
    assertThat(newSettingsService.getDirectoryDirectoryChooserInitDirectory()).isEqualTo(expectedAddDirectoryDirectoryChooserDirectory);
    assertThat(newSettingsService.getAddTrackFileChooserInitDirectory()).isEqualTo(expectedAddTrackFileChooserDirectory);
    assertThat(newSettingsService.getOpenFileFileChooserInitDirectory()).isEqualTo(expectedOpenFileFileChooserDirectory);
    assertThat(newSettingsService.getLastFmSettings()).isEqualTo(expectedLastFmSettings);
  }

  @Test
  @DisplayName("update settings if the file already exists")
  void updateSettingsIfFileExists() {
    var initVolumeValue = 0.3;
    var initAddTrackFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Affinity");
    var initAddDirectoryDirectoryChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Virus");
    var initOpenFileFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Vector");
    var initLastFmSettings = new LastFmSettings(true, false);

    var settingsService = new SettingsService(fileSettingsStorage);
    settingsService.setVolume(initVolumeValue);
    settingsService.setAddTrackFileChooserInitDirectory(initAddTrackFileChooserDirectory);
    settingsService.setAddDirectoryDirectoryChooserInitDirectory(initAddDirectoryDirectoryChooserDirectory);
    settingsService.setOpenFileFileChooserInitDirectory(initOpenFileFileChooserDirectory);
    settingsService.setLastFmSettings(initLastFmSettings);
    settingsService.onCloseApplicationEvent(new CloseApplicationEvent());

    var finalVolumeValue = 0.5;
    var finalAddTrackFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Affinity");
    var finalAddDirectoryDirectoryChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Virus");
    var finalOpenFileFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Restoration");
    var finalLastFmSettings = new LastFmSettings(true, true);

    var newSettingsService = new SettingsService(fileSettingsStorage);
    newSettingsService.setVolume(finalVolumeValue);
    newSettingsService.setAddDirectoryDirectoryChooserInitDirectory(finalAddDirectoryDirectoryChooserDirectory);
    newSettingsService.setAddTrackFileChooserInitDirectory(finalAddTrackFileChooserDirectory);
    newSettingsService.setOpenFileFileChooserInitDirectory(finalOpenFileFileChooserDirectory);
    newSettingsService.setLastFmSettings(finalLastFmSettings);
    newSettingsService.onCloseApplicationEvent(new CloseApplicationEvent());

    var finalSettingsService = new SettingsService(fileSettingsStorage);
    assertThat(finalSettingsService.getVolume()).isEqualTo(finalVolumeValue);
    assertThat(finalSettingsService.getAddTrackFileChooserInitDirectory()).isEqualTo(finalAddTrackFileChooserDirectory);
    assertThat(finalSettingsService.getDirectoryDirectoryChooserInitDirectory()).isEqualTo(finalAddDirectoryDirectoryChooserDirectory);
    assertThat(finalSettingsService.getOpenFileFileChooserInitDirectory()).isEqualTo(finalOpenFileFileChooserDirectory);
    assertThat(finalSettingsService.getLastFmSettings()).isEqualTo(finalLastFmSettings);
  }

  @AfterEach
  void removeSettingsAfterTest() throws Exception {
    Files.deleteIfExists(settingsFile);
  }
  
  private SettingsStorage settingsStorage(Path tempFile) {
    return  new JsonFileSettingsStorage(TestUtils.objectMapper(), tempFile);
  }

  private Path getTempFile() {
    return Paths.get(FileUtils.getTempDirectory(), "settings-" + Instant.now().toEpochMilli() + ".json");
  }

}
