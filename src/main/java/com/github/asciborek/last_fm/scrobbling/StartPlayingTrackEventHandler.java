package com.github.asciborek.last_fm.scrobbling;

import static com.github.asciborek.last_fm.scrobbling.TrackApiErrorCode.SERVICE_OFFLINE;
import static com.github.asciborek.last_fm.scrobbling.TrackApiErrorCode.TEMPORARY_ERROR;

import com.github.asciborek.last_fm.LastFmUserService;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.github.asciborek.util.AutoRegistrableEventBusListener;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.event.ExecutionAttemptedEvent;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoRegistrableEventBusListener
public final class StartPlayingTrackEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(StartPlayingTrackEventHandler.class);
  private static final Set<TrackApiErrorCode> TEMPORARY_ERRORS = Set.of(SERVICE_OFFLINE, TEMPORARY_ERROR);

  private final LastFmUserService lastFmUserService;
  private final TrackApiService trackApiService;
  private final ExecutorService executorService;
  private final RetryPolicy<NowPlayingResponse> retryPolicy;


  @Inject
  @SuppressWarnings("unused")
  public StartPlayingTrackEventHandler(LastFmUserService lastFmUserService,
      TrackApiService trackApiService, ExecutorService executorService) {
    this(lastFmUserService, trackApiService, executorService, Duration.ofSeconds(3), Duration.ofSeconds(12));
  }

  StartPlayingTrackEventHandler(LastFmUserService lastFmUserService,
      TrackApiService trackApiService, ExecutorService executorService, Duration delay, Duration maxDuration) {
    this.lastFmUserService = lastFmUserService;
    this.trackApiService = trackApiService;
    this.executorService = executorService;
    this.retryPolicy = retryPolicy(delay, maxDuration);
  }

  @Subscribe
  public void onStartPlayingTrackEvent(StartPlayingTrackEvent event) {
    LOG.info("Received start playing track event: {}", event);
    lastFmUserService.getUserSession().ifPresent(session -> Failsafe.with(retryPolicy)
        .with(executorService)
        .getAsync(() -> trackApiService.sendUpdateNowPlayingRequest(event.track(), session.token()))
        .thenAccept(nowPlayingResponse -> LOG.info("update now playing response: {}", nowPlayingResponse)));
  }

  private RetryPolicy<NowPlayingResponse> retryPolicy(Duration delay, Duration maxDuration) {
    return RetryPolicy.<NowPlayingResponse>builder()
        .withDelay(delay)
        .handle(RuntimeException.class)
        .handleResultIf(this::isTemporaryError)
        .withMaxAttempts(5)
        .withMaxDuration(maxDuration)
        .onRetry(this::logRetry)
        .build();
  }

  private boolean isTemporaryError(NowPlayingResponse nowPlayingResponse) {
    if (nowPlayingResponse instanceof NowPlayingResponse.ErrorResponse errorResponse) {
      return TEMPORARY_ERRORS.contains(errorResponse.error());
    }
    return false;
  }

  private void logRetry(ExecutionAttemptedEvent<NowPlayingResponse> event) {
    if (event.getLastException() != null) {
      LOG.error("Retry updating now playing after exception {}, attempt count: {}", event.getLastException(), event.getAttemptCount());
    }
  }

}
