package com.github.asciborek.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
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

  @Inject
  public SettingsServiceFactory(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  private ObjectMapper objectMapper() {
    var mapper = new ObjectMapper();
    mapper.registerModule(new Jdk8Module());
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return  mapper;
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
