package com.github.asciborek.last_fm.scrobbling;

import com.github.asciborek.last_fm.LastFmUserService;
import com.github.asciborek.player.PlayerEvent.TrackPlayedEvent;
import com.github.asciborek.util.AutoRegistrableEventBusListener;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoRegistrableEventBusListener
public final class TrackPlayedEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(TrackPlayedEventHandler.class);

  private final LastFmUserService lastFmUserService;
  private final ScrobblesDao scrobblesDao;
  private final ExecutorService executorService;

  @Inject
  public TrackPlayedEventHandler(LastFmUserService lastFmUserService,
      ScrobblesDao scrobblesDao, ExecutorService executorService) {
    this.lastFmUserService = lastFmUserService;
    this.scrobblesDao = scrobblesDao;
    this.executorService = executorService;
  }

  @Subscribe
  public void onTrackPlayedEvent(TrackPlayedEvent event) {
    LOG.info("Received track played event: {}", event);
    if (shouldInsertScrobble()) {
      Scrobble scrobble = toScrobble(event);
      executorService.submit(() -> scrobblesDao.insertScrobble(scrobble));
    } else {
      LOG.info("Scrobbling disabled, not saving scrobble to outbox table");
    }
  }

  private Scrobble toScrobble(TrackPlayedEvent trackPlayedEvent) {
    return new Scrobble(trackPlayedEvent.track().artist(),
        trackPlayedEvent.track().album(),
        trackPlayedEvent.track().title(),
        trackPlayedEvent.timestamp());
  }

  private boolean shouldInsertScrobble() {
    return lastFmUserService.getLastFmSettings().scrobblingEnabled();
  }

}
