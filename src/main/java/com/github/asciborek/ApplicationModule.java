package com.github.asciborek;

import com.github.asciborek.album_cover.AlbumCoverController;
import com.github.asciborek.album_cover.AlbumCoverControllerFactory;
import com.github.asciborek.artist_info.ArtistInfoController;
import com.github.asciborek.artist_info.ArtistInfoControllerFactory;
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
    bind(AlbumCoverController.class).toProvider(AlbumCoverControllerFactory.class).in(Scopes.SINGLETON);
    bind(ArtistInfoController.class).toProvider(ArtistInfoControllerFactory.class).in(Scopes.SINGLETON);
    install(new MetadataModule());
  }

  private ExecutorService executorService() {
    LOG.info("create executor service, available processors: {}", AVAILABLE_PROCESSORS);
    return Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
  }
  
}
