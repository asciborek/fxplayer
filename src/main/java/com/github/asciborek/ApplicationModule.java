package com.github.asciborek;

import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.settings.SettingsServiceProvider;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationModule.class);
  private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();


  @Override
  protected void configure() {
    bind(SettingsService.class).toProvider(SettingsServiceProvider.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public ExecutorService executorService() {
    LOG.info("create executor service, available processors: {}", AVAILABLE_PROCESSORS);
    return Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
  }

  @Provides
  @Singleton
  @SuppressWarnings("UnstableApiUsage")
  public EventBus eventBus() {
    return new EventBus();
  }

}
