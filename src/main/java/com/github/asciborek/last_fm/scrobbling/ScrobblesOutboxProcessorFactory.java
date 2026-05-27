package com.github.asciborek.last_fm.scrobbling;

import com.github.asciborek.last_fm.LastFmUserService;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.concurrent.ScheduledExecutorService;

public class ScrobblesOutboxProcessorFactory implements Provider<ScrobblesOutboxProcessor> {

  private final ScheduledExecutorService scheduledExecutorService;
  private final LastFmUserService lastFmUserService;
  private final ScrobblesDao scrobblesDao;
  private final TrackApiService trackApiService;
  private final EventBus eventBus;

  @Inject
  public ScrobblesOutboxProcessorFactory(ScheduledExecutorService scheduledExecutorService,
      LastFmUserService lastFmUserService, ScrobblesDao scrobblesDao,
      TrackApiService trackApiService, EventBus eventBus) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.lastFmUserService = lastFmUserService;
    this.scrobblesDao = scrobblesDao;
    this.trackApiService = trackApiService;
    this.eventBus = eventBus;
  }

  @Override
  public ScrobblesOutboxProcessor get() {
    ScrobblesOutboxProcessor outBoxProcessor = new ScrobblesOutboxProcessor(scheduledExecutorService, lastFmUserService, scrobblesDao, trackApiService);
    outBoxProcessor.init();
    eventBus.register(outBoxProcessor);
    return outBoxProcessor;
  }
}
