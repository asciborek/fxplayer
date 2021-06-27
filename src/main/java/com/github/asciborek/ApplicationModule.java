package com.github.asciborek;

import com.github.asciborek.album_cover.AlbumCoverProvider;
import com.github.asciborek.album_cover.AlbumCoverProviderFactory;
import com.github.asciborek.artist_info.ArtistInfoProvider;
import com.github.asciborek.artist_info.ArtistInfoProviderFactory;
import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.settings.SettingsServiceFactory;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApplicationModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationModule.class);
  private static final String API_KEYS_FILE_NAME =  "api_keys.properties";
  private static final String API_KEY_PROPERTY_NAME = "last.fm";
  private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

  @Override
  @SuppressWarnings("UnstableApiUsage")
  protected void configure() {
    bind(EventBus.class).toInstance(new EventBus());
    bind(ExecutorService.class).toProvider(this::executorService).in(Scopes.SINGLETON);
    bind(SettingsService.class).toProvider(SettingsServiceFactory.class).in(Scopes.SINGLETON);
    bind(ArtistInfoProvider.class).toProvider(ArtistInfoProviderFactory.class).in(Scopes.SINGLETON);
    bind(AlbumCoverProvider.class).toProvider(AlbumCoverProviderFactory.class).in(Scopes.SINGLETON);
  }

  private ExecutorService executorService() {
    LOG.info("create executor service, available processors: {}", AVAILABLE_PROCESSORS);
    return Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
  }

  @Provides
  @Singleton
  public String lastFmAPiKey() {
    return openProperties().getProperty(API_KEY_PROPERTY_NAME);
  }

  @Provides
  @Singleton
  public HttpClient lastFmHttpClient() {
    return HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
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
