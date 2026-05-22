package com.github.asciborek.last_fm.scrobbling;

import com.github.asciborek.last_fm.LastFmUserService;
import com.github.asciborek.player.PlayerEvent.TrackPlayedEvent;
import com.github.asciborek.util.AutoRegistrableEventBusListener;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoRegistrableEventBusListener
public final class TrackPlayedEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(TrackPlayedEventHandler.class);

  private final LastFmUserService lastFmUserService;
  private final TrackApiService trackApiService;
  private final ExecutorService executorService;

  @Inject
  public TrackPlayedEventHandler(LastFmUserService lastFmUserService, TrackApiService trackApiService,
      ExecutorService executorService) {
    this.lastFmUserService = lastFmUserService;
    this.trackApiService = trackApiService;
    this.executorService = executorService;
  }

  @Subscribe
  public void onTrackPlayedEvent(TrackPlayedEvent event) {
    LOG.info("Received track played event: {}", event);
    lastFmUserService.getUserSession().ifPresent(session -> {
      var scrobbles = List.of(toScrobble(event));
      CompletableFuture.supplyAsync(() -> trackApiService.sendScrobbleTracksRequest(scrobbles, session.token()), executorService)
          .thenAccept(scrobbleResponse -> LOG.info("scrobble track response : {}", scrobbleResponse));
    });
  }

  private Scrobble toScrobble(TrackPlayedEvent trackPlayedEvent) {
    return new Scrobble(trackPlayedEvent.track().artist(),
        trackPlayedEvent.track().title(),
        trackPlayedEvent.track().album(),
        trackPlayedEvent.timestamp().getEpochSecond());
  }

}
