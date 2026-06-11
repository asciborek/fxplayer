package com.github.asciborek.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JsonFileSettingsStorage implements SettingsStorage {

  private static final Logger LOG = LoggerFactory.getLogger(JsonFileSettingsStorage.class);
  private final ObjectMapper objectMapper;
  private final Path configFilePath;

  public JsonFileSettingsStorage(ObjectMapper objectMapper, Path configFilePath) {
    this.objectMapper = objectMapper;
    this.configFilePath = configFilePath;
  }

  @Override
  public void save(Settings settings) {
    SettingsSnapshot settingsSnapshot = mapToSnapshot(settings);
    try (var writer = Files.newBufferedWriter(configFilePath)) {
      objectMapper.writeValue(writer, settingsSnapshot);
    } catch (Exception e) {
      LOG.error("save the settings an error occurred {}", e.getMessage(), e);
    }
  }

  @Override
  public Optional<Settings> loadSettings() {
    if (!Files.exists(configFilePath)) {
      return Optional.empty();
    }
    try(var reader = Files.newBufferedReader(configFilePath)) {
      var settingsSnapshot = objectMapper.readValue(reader, SettingsSnapshot.class);
      return Optional.of(readFromSnapshot(settingsSnapshot));
    } catch (Exception e) {
      LOG.error("load the settings an error occurred {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    return "JsonFileSettingsStorage";
  }

  private Settings readFromSnapshot(SettingsSnapshot snapshot) {
    return new Settings(snapshot.coreSettings().volumeLevel(),
        snapshot.coreSettings().addDirectoryDirectoryChooserInitDirectory(),
        snapshot.coreSettings().addTrackFileChooserInitDirectory(),
        snapshot.coreSettings().openFileFileChooserInitDirectory(),
        snapshot.lastFmSettings());
  }

  private SettingsSnapshot mapToSnapshot(Settings settings) {
    return new SettingsSnapshot(
        new CoreSettingsSnapshot(settings.getVolumeLevel().orElse(null),
        settings.getAddDirectoryDirectoryChooserInitDirectory().orElse(null),
        settings.getAddTrackFileChooserInitDirectory().orElse(null),
        settings.getOpenFileFileChooserInitDirectory().orElse(null)),
        settings.getLastFmSettings().orElse(null)
    );
  }

  public record SettingsSnapshot(CoreSettingsSnapshot coreSettings, LastFmSettings lastFmSettings) {}

  public record CoreSettingsSnapshot(Double volumeLevel,
                                     String addDirectoryDirectoryChooserInitDirectory,
                                     String addTrackFileChooserInitDirectory,
                                     String openFileFileChooserInitDirectory) {}

}
