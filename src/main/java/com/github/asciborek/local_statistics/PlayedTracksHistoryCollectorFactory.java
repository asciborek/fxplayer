package com.github.asciborek.local_statistics;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayedTracksHistoryCollectorFactory implements Provider<PlayedTracksHistoryCollector> {

  private static final Logger LOG = LoggerFactory.getLogger(PlayedTracksHistoryCollectorFactory.class);

  private final EventBus eventBus;
  private final ExecutorService executorService;
  private final DataSource dataSource;

  @Inject
  public PlayedTracksHistoryCollectorFactory(EventBus eventBus, ExecutorService executorService, DataSource dataSource) {
    this.eventBus = eventBus;
    this.executorService = executorService;
    this.dataSource = dataSource;
  }

  @Override
  public PlayedTracksHistoryCollector get() {
    var collector = new PlayedTracksHistoryCollector(executorService, dataSource);
    eventBus.register(collector);
    LOG.info("TrackPlayedLocalStatisticsCollector created");
    return collector;
  }
}
