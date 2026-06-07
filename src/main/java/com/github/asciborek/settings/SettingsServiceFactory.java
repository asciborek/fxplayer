package com.github.asciborek.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.asciborek.util.FileUtils;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
public final class SettingsServiceFactory implements Provider<SettingsService> {

  private static final Logger LOG = LoggerFactory.getLogger(SettingsServiceFactory.class);
  private static final String SETTINGS_FILENAME = "settings.json";


  private final EventBus eventBus;
  private final ObjectMapper objectMapper;

  @Inject
  public SettingsServiceFactory(EventBus eventBus, ObjectMapper objectMapper) {
    this.eventBus = eventBus;
    this.objectMapper = objectMapper;
  }

  @Override
  public SettingsService get() {
    LOG.info("creating SettingsService...");
    var settingsService =  new SettingsService(new JsonFileSettingsStorage(objectMapper, settingsFilePath()));
    eventBus.register(settingsService);
    return settingsService;
  }

  private Path settingsFilePath() {
    return FileUtils.getApplicationDataDirectory().resolve(SETTINGS_FILENAME);
  }
}
