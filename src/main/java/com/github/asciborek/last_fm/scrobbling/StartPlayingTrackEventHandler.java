package com.github.asciborek.last_fm.scrobbling;

import com.github.asciborek.last_fm.LastFmUserService;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.github.asciborek.util.AutoRegistrableEventBusListener;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoRegistrableEventBusListener
public final class StartPlayingTrackEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(StartPlayingTrackEventHandler.class);

  private final LastFmUserService lastFmUserService;
  private final TrackApiService trackApiService;
  private final ExecutorService executorService;

  @Inject
  public StartPlayingTrackEventHandler(LastFmUserService lastFmUserService,
      TrackApiService trackApiService, ExecutorService executorService) {
    this.lastFmUserService = lastFmUserService;
    this.trackApiService = trackApiService;
    this.executorService = executorService;
  }

  @Subscribe
  public void onStartPlayingTrackEvent(StartPlayingTrackEvent event) {
    LOG.info("Received start playing track event: {}", event);
    lastFmUserService.getUserSession().ifPresent(session -> {
      CompletableFuture.supplyAsync(() -> trackApiService.sendUpdateNowPlayingRequest(event.track(), session.token()), executorService)
          .thenAccept(nowPlayingResponse -> LOG.info("update now playing response: {}", nowPlayingResponse));
    });
  }

}
