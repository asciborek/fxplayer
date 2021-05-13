package com.github.asciborek.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.asciborek.util.FileUtils;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
public final class SettingsServiceProvider implements Provider<SettingsService> {

  private static final Logger LOG = LoggerFactory.getLogger(SettingsServiceProvider.class);
  private static final String SETTINGS_FILENAME = "settings.json";

  private final EventBus eventBus;

  @Inject
  public SettingsServiceProvider(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  private ObjectMapper objectMapper() {
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    return  objectMapper;
  }

  private Path settingsFilePath() {
    return FileUtils.getApplicationDataDirectory().resolve(SETTINGS_FILENAME);
  }

  @Override
  public SettingsService get() {
    LOG.info("creating SettingsService...");
    var settingsStorage = new JsonFileSettingsStorage(objectMapper(), settingsFilePath());
    var settingsService =  new SettingsService(settingsStorage);
    eventBus.register(settingsService);
    return settingsService;
  }
}
