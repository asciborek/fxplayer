package com.github.asciborek.artist_info;

import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public final class ArtistInfoProviderFactory implements Provider<ArtistInfoProvider> {

  private static final String API_KEYS_FILE_NAME =  "api_keys.properties";
  private static final String API_KEY_PROPERTY_NAME = "last.fm";

  private final HttpClient httpClient;
  private final String lastFmApiKey;

  @Inject
  public ArtistInfoProviderFactory(ExecutorService executorService) {
    lastFmApiKey = openProperties().getProperty(API_KEY_PROPERTY_NAME);
    httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .executor(executorService)
        .build();
  }

  @Override
  public ArtistInfoProvider get() {
    var lastFmArtistInfoProvider = new LastFmArtistInfoProvider(httpClient, lastFmApiKey);
    return new CachingArtistInfoProvider(lastFmArtistInfoProvider);
  }

  private Properties openProperties() {
    var properties = new Properties();
    try (InputStream inputStream = Resources.getResource(API_KEYS_FILE_NAME).openStream()) {
      properties.load(inputStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return properties;
  }
}
