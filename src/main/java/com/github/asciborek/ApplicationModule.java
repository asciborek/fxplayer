package com.github.asciborek;

import com.github.asciborek.album_cover.AlbumCoverModule;
import com.github.asciborek.artist_info.ArtistInfoModule;
import com.github.asciborek.metadata.MetadataModule;
import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.settings.SettingsServiceFactory;
import com.github.asciborek.util.DeadEventLoggingListener;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ApplicationModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationModule.class);
  private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

  @Override
  @SuppressWarnings("UnstableApiUsage")
  protected void configure() {
    bind(EventBus.class).toInstance(new EventBus());
    bind(ExecutorService.class).toProvider(this::executorService).in(Scopes.SINGLETON);
    bind(DeadEventLoggingListener.class).asEagerSingleton();
    bind(SettingsService.class).toProvider(SettingsServiceFactory.class).in(Scopes.SINGLETON);
    install(new MetadataModule());
    install(new AlbumCoverModule());
    install(new ArtistInfoModule());
  }

  private ExecutorService executorService() {
    LOG.info("create executor service, available processors: {}", AVAILABLE_PROCESSORS);
    return Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
  }

}
