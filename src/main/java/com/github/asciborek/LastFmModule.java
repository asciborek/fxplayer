package com.github.asciborek;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;

final class LastFmModule extends AbstractModule {

  private final Properties lastFmProperties;

  public LastFmModule(Properties lastFmProperties) {
    this.lastFmProperties = lastFmProperties;
  }

  private static final String API_KEY_PROPERTY_NAME = "api_key";

  @Override
  protected void configure() {
    bind(HttpClient.class).toProvider(this::lastFmHttpClient).in(Scopes.SINGLETON);
    bind(String.class).toInstance(lastFmProperties.getProperty(API_KEY_PROPERTY_NAME));
  }

  public HttpClient lastFmHttpClient() {
    return HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }

}
