package com.github.asciborek.local_statistics;

import com.github.asciborek.util.TimeProvider;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;

final class LocalTrackStatisticsControllerFactory implements Provider<LocalTrackStatisticsController> {

  private final DataSource dataSource;
  private final ExecutorService executorService;
  private final TimeProvider timeProvider;
  private final DateTimeFormatter dateTimeFormatter;
  private final EventBus eventBus;

  @Inject
  LocalTrackStatisticsControllerFactory(DataSource dataSource, ExecutorService executorService,
      TimeProvider timeProvider, DateTimeFormatter dateTimeFormatter, EventBus eventBus) {
    this.dataSource = dataSource;
    this.executorService = executorService;
    this.timeProvider = timeProvider;
    this.dateTimeFormatter = dateTimeFormatter;
    this.eventBus = eventBus;
  }

  @Override
  public LocalTrackStatisticsController get() {
    var trackLocalStatisticsProvider = new TrackLocalStatisticsProvider(executorService, dataSource);
    var localTrackStatisticsController = new LocalTrackStatisticsController(trackLocalStatisticsProvider, timeProvider, dateTimeFormatter);
    eventBus.register(localTrackStatisticsController);
    return localTrackStatisticsController;
  }
}
