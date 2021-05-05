package com.github.asciborek.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.asciborek.util.FileUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JsonFileSettingsStorage implements SettingsStorage {

  private static final Logger LOG = LoggerFactory.getLogger(SettingsStorage.class);
  private final ObjectMapper objectMapper;
  private final Path configFilePath;

  public JsonFileSettingsStorage(ObjectMapper objectMapper, Path configFilePath) {
    this.objectMapper = objectMapper;
    this.configFilePath = configFilePath;
  }

  @Override
  public void save(Settings settings) {
    if (!Files.exists(configFilePath)) {
      createConfigFile();
    }
    try (var writer = Files.newBufferedWriter(configFilePath)) {
      objectMapper.writeValue(writer, settings);
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
      var settings = objectMapper.readValue(reader, Settings.class);
      return Optional.of(settings);
    } catch (Exception e) {
      LOG.error("load the settings an error occurred {}", e.getMessage(), e);
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    return "JsonFileSettingsStorage";
  }

  private void createConfigFile() {
    LOG.info("Settings file {} does not exist, creating one...", configFilePath);
    var directory = configFilePath.getParent();
    if (!Files.exists(directory)) {
      FileUtils.createDirectory(directory);
    }
    FileUtils.createFile(configFilePath);
  }
}
