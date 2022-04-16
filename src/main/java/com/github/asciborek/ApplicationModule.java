package com.github.asciborek;

import com.github.asciborek.album_cover.AlbumCoverController;
import com.github.asciborek.album_cover.AlbumCoverControllerFactory;
import com.github.asciborek.artist_info.ArtistInfoController;
import com.github.asciborek.artist_info.ArtistInfoControllerFactory;
import com.github.asciborek.local_statistics.LocalStatisticsModule;
import com.github.asciborek.metadata.MetadataModule;
import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.settings.SettingsServiceFactory;
import com.github.asciborek.util.DeadEventLoggingListener;
import com.github.asciborek.util.FileUtils;
import com.github.asciborek.util.SystemTimeProvider;
import com.github.asciborek.util.TimeProvider;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ApplicationModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationModule.class);
  private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
  private static final String DB_FILENAME = "fx-player.db";
  private static final String DEFAULT_DATE_TIME_FORMAT = "dd MMMM yy hh:mm a";

  @Override
  @SuppressWarnings("UnstableApiUsage")
  protected void configure() {
    bind(TimeProvider.class).toInstance(new SystemTimeProvider());
    bind(DateTimeFormatter.class).toProvider(this::dateTimeFormatter).in(Scopes.SINGLETON);
    bind(ExecutorService.class).toProvider(this::executorService).in(Scopes.SINGLETON);
    bind(DataSource.class).toProvider(this::dataSource).asEagerSingleton();
    bind(EventBus.class).toInstance(new EventBus());
    bind(DeadEventLoggingListener.class).asEagerSingleton();
    bind(SettingsService.class).toProvider(SettingsServiceFactory.class).in(Scopes.SINGLETON);
    bind(AlbumCoverController.class).toProvider(AlbumCoverControllerFactory.class).in(Scopes.SINGLETON);
    bind(ArtistInfoController.class).toProvider(ArtistInfoControllerFactory.class).in(Scopes.SINGLETON);

    install(new MetadataModule());
    install(new LocalStatisticsModule());
  }

  private DateTimeFormatter dateTimeFormatter() {
    return DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
  }

  private ExecutorService executorService() {
    LOG.info("create executor service, available processors: {}", AVAILABLE_PROCESSORS);
    return Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
  }

  private DataSource dataSource() {
    var dataSource = dataSource(dataSourceUrl());
    runMigration(dataSource);
    return dataSource;
  }

  private String dataSourceUrl() {
    Path dbFile = FileUtils.getApplicationDataDirectory().resolve(DB_FILENAME);
    return "jdbc:sqlite:" + dbFile;
  }

  private void runMigration(DataSource dataSource) {
    LOG.info("Start running migration");
    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .load();
    flyway.migrate();
    LOG.info("Flyway migration has been finished");
  }

  private DataSource dataSource(String jdbcUrl) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setMaximumPoolSize(3);
    return new HikariDataSource(config);
  }
  
}
