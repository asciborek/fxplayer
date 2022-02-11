package com.github.asciborek.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.asciborek.util.FileUtils;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
public final class SettingsServiceFactory implements Provider<SettingsService> {

  private static final Logger LOG = LoggerFactory.getLogger(SettingsServiceFactory.class);

  private final EventBus eventBus;
  private final DataSource dataSource;

  @Inject
  public SettingsServiceFactory(EventBus eventBus, DataSource dataSource) {
    this.eventBus = eventBus;
    this.dataSource = dataSource;
  }

  @Override
  public SettingsService get() {
    LOG.info("creating SettingsService...");
    var settingsService =  new SettingsService(new SqliteSettingsStorage(dataSource));
    eventBus.register(settingsService);
    return settingsService;
  }
}
