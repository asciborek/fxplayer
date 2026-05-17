package com.github.asciborek.last_fm.scrobbling;

import com.github.asciborek.last_fm.LastFmUserService;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.github.asciborek.player.PlayerEvent.TrackPlayedEvent;
import com.google.common.eventbus.Subscribe;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TrackEventsHandler {

  private static final Logger LOG = LoggerFactory.getLogger(TrackEventsHandler.class);

  private final LastFmUserService lastFmUserService;
  private final TrackApiClient trackApiClient;
  private final ExecutorService executorService;

  public TrackEventsHandler(LastFmUserService lastFmUserService, TrackApiClient trackApiClient,
      ExecutorService executorService) {
    this.lastFmUserService = lastFmUserService;
    this.trackApiClient = trackApiClient;
    this.executorService = executorService;
  }

  @Subscribe
  public void onStartPlayingTrackEvent(StartPlayingTrackEvent event) {
    LOG.info("Received start playing track event: {}", event);
    lastFmUserService.getUserSession().ifPresent(session -> {
      CompletableFuture.supplyAsync(() -> trackApiClient.sendUpdateNowPlayingRequest(event.track(), session.token()), executorService)
          .thenAccept(nowPlayingResponse -> LOG.info("update now playing response: {}", nowPlayingResponse));
    });
  }

  @Subscribe
  public void onTrackPlayedEvent(TrackPlayedEvent event) {
    LOG.info("Received track played event: {}", event);
    lastFmUserService.getUserSession().ifPresent(session -> {
      var scrobbles = List.of(toScrobble(event));
      CompletableFuture.supplyAsync(() -> trackApiClient.sendScrobbleTracksRequest(scrobbles, session.token()))
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
