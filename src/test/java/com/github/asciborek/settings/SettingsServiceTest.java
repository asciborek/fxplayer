package com.github.asciborek.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.asciborek.util.FileUtils;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SettingsServiceTest {

  @Test
  @DisplayName("should load default settings if settings file does not exist")
  void shouldLoadDefaultSettingsIfSettingsFileDoesNotExist() {
    var tempFile= getTempFile();
    var settingsService = new SettingsService(settingsStorage(tempFile));

    //default volume value is max
    Assertions.assertEquals(SettingsService.MAX_VOLUME_LEVEL, settingsService.getVolume());

    Assertions.assertEquals(new File(FileUtils.getUserHome()), settingsService.getAddTrackChoicePath());
    Assertions.assertEquals(new File(FileUtils.getUserHome()), settingsService.getAddDirectoryChoicePath());
  }

  @Test
  @DisplayName("should  save and read settings")
  void shouldSaveAndReadSettings() {
    var expectedVolumeValue = 0.3;
    var expectedAddTrackInitPath = new File(FileUtils.getUserHome() + "/Music/Haken/Affinity");
    var expectedAddDirectoryInitPath = new File(FileUtils.getUserHome() + "/Music/Haken/Virus");

    var tempFile= getTempFile();
    var settingsService = new SettingsService(settingsStorage(tempFile));
    settingsService.setVolume(expectedVolumeValue);
    settingsService.setAddTrackChoicePath(expectedAddTrackInitPath);
    settingsService.setAddDirectoryChoicePath(expectedAddDirectoryInitPath);

    var newSettingsService = new SettingsService(settingsStorage(tempFile));
    Assertions.assertEquals(expectedVolumeValue, newSettingsService.getVolume());
    Assertions.assertEquals(expectedAddDirectoryInitPath, newSettingsService.getAddDirectoryChoicePath());
    Assertions.assertEquals(expectedAddTrackInitPath, newSettingsService.getAddTrackChoicePath());
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
