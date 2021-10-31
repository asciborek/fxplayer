package com.github.asciborek.settings;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.util.FileUtils;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SettingsServiceTest {

  @Test
  @DisplayName("should load default settings if settings file does not exist")
  void shouldLoadDefaultSettingsIfSettingsFileDoesNotExist() {
    var tempFile= getTempFile();
    var settingsService = new SettingsService(settingsStorage(tempFile));

    //default volume value is max
    assertThat(SettingsService.MAX_VOLUME_LEVEL).isEqualTo(settingsService.getVolume());
    //default directory should be home directory
    assertThat(settingsService.getAddTrackFileChooserInitDirectory()).isEqualTo(new File(FileUtils.getUserHome()));
    assertThat(settingsService.getDirectoryDirectoryChooserInitDirectory()).isEqualTo(new File(FileUtils.getUserHome()));
    assertThat(settingsService.getOpenFileFileChooserInitDirectory()).isEqualTo(new File(FileUtils.getUserHome()));
  }

  @Test
  @DisplayName("should  save and read settings")
  void shouldSaveAndReadSettings() {
    var expectedVolumeValue = 0.3;
    var expectedAddTrackFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Affinity");
    var expectedAddDirectoryDirectoryChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Virus");
    var expectedOpenFileFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Vector");

    var tempFile= getTempFile();
    var settingsService = new SettingsService(settingsStorage(tempFile));
    settingsService.setVolume(expectedVolumeValue);
    settingsService.setAddTrackFileChooserInitDirectory(expectedAddTrackFileChooserDirectory);
    settingsService.setAddDirectoryDirectoryChooserInitDirectory(expectedAddDirectoryDirectoryChooserDirectory);
    settingsService.setOpenFileFileChooserInitDirectory(expectedOpenFileFileChooserDirectory);
    settingsService.onCloseApplicationEvent(new CloseApplicationEvent());
    var newSettingsService = new SettingsService(settingsStorage(tempFile));
    assertThat(newSettingsService.getVolume()).isEqualTo(expectedVolumeValue);
    assertThat(newSettingsService.getDirectoryDirectoryChooserInitDirectory()).isEqualTo(expectedAddDirectoryDirectoryChooserDirectory);
    assertThat(newSettingsService.getAddTrackFileChooserInitDirectory()).isEqualTo(expectedAddTrackFileChooserDirectory);
    assertThat(newSettingsService.getOpenFileFileChooserInitDirectory()).isEqualTo(expectedOpenFileFileChooserDirectory);
  }

  private SettingsStorage settingsStorage(Path tempFile) {
    return  new JsonFileSettingsStorage(objectMapper(), tempFile);
  }

  private Path getTempFile() {
    return Paths.get(FileUtils.getTempDirectory(), "settings-" + Instant.now().toEpochMilli() + ".json");
  }

  private ObjectMapper objectMapper() {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    return  objectMapper;
  }

}
