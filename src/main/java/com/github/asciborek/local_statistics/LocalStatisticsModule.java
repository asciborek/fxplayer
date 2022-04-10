package com.github.asciborek.local_statistics;

import com.google.inject.PrivateModule;

public class LocalStatisticsModule extends PrivateModule {

  @Override
  protected void configure() {
    bind(PlayedTracksHistoryCollector.class).toProvider(PlayedTracksHistoryCollectorFactory.class);
  }
}
