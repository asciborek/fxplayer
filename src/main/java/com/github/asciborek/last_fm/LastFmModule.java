package com.github.asciborek.last_fm;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;

public final class LastFmModule extends AbstractModule {

  private static final String API_KEY_PROPERTY_NAME = "api_key";
  private final EventBus eventBus;
  private final Properties lastFmProperties;

  public LastFmModule(EventBus eventBus, Properties lastFmProperties) {
    this.lastFmProperties = lastFmProperties;
    this.eventBus = eventBus;
  }

  @Override
  protected void configure() {
    bind(HttpClient.class).toProvider(this::lastFmHttpClient).in(Scopes.SINGLETON);
    bind(String.class).annotatedWith(Names.named("lastFmApiKey")).toInstance(lastFmProperties.getProperty(API_KEY_PROPERTY_NAME));
    bind(OpenLastFmSettingsCommandHandler.class).toProvider(this::openLastFmSettingsCommandHandler).asEagerSingleton();
  }

  public HttpClient lastFmHttpClient() {
    return HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }

  private OpenLastFmSettingsCommandHandler openLastFmSettingsCommandHandler() {
    var openLastFmSettingsCommandHandler = new OpenLastFmSettingsCommandHandler();
    eventBus.register(openLastFmSettingsCommandHandler);
    return openLastFmSettingsCommandHandler;
  }

}
