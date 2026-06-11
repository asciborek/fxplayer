package com.github.asciborek.last_fm.scrobbling;

import static com.github.asciborek.last_fm.scrobbling.TrackApiErrorCode.INVALID_SESSION_KEY;

import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.last_fm.InvalidSessionKeyEvent;
import com.github.asciborek.last_fm.LastFmSettingsChangedEvent;
import com.github.asciborek.last_fm.LastFmUserService;
import com.github.asciborek.last_fm.UserSession;
import com.github.asciborek.last_fm.scrobbling.ScrobbleResponse.ErrorResponse;
import com.github.asciborek.last_fm.scrobbling.ScrobbleResponse.SuccessResponse;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrobblesOutboxProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ScrobblesOutboxProcessor.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final EventBus eventBus;
  private final LastFmUserService lastFmUserService;
  private final ScrobblesDao scrobblesDao;
  private final TrackApiService trackApiService;

  private ScheduledFuture<?> processScrobblesFuture;
  private boolean isScheduled = false;

  public ScrobblesOutboxProcessor(ScheduledExecutorService scheduledExecutorService,
      EventBus eventBus,
      LastFmUserService lastFmUserService,
      ScrobblesDao scrobblesDao,
      TrackApiService trackApiService) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.eventBus = eventBus;
    this.lastFmUserService = lastFmUserService;
    this.scrobblesDao = scrobblesDao;
    this.trackApiService = trackApiService;
  }

  public void init() {
    if (lastFmUserService.isOnlineScrobblingEnabled()) {
      scheduleProcessing();
    }
  }

  @Subscribe
  public void onLastFmSettingsChanged(LastFmSettingsChangedEvent settingsChangedEvent) {
    LOG.info("LastFm settings changed {}", settingsChangedEvent);
    if (lastFmUserService.isOnlineScrobblingEnabled() && (!isScheduled)) {
      scheduleProcessing();
    } else if (lastFmUserService.isOnlineScrobblingDisabled() && isScheduled) {
      LOG.info("Online scrobbling disabled, cancelling processScrobblesFuture");
      isScheduled = false;
      if (processScrobblesFuture != null) {
        processScrobblesFuture.cancel(false);
      }
    }
  }

  @Subscribe
  public void cancelTaskOnCloseApplicationEvent(CloseApplicationEvent event) {
    LOG.info("received CloseApplicationEvent, cancelling processScrobblesFuture");
    if (processScrobblesFuture != null) {
      processScrobblesFuture.cancel(false);
    }
  }

  private void scheduleProcessing() {
    isScheduled = true;
    processScrobblesFuture = scheduledExecutorService.scheduleAtFixedRate(this::processScrobbles, 2L, 30L, TimeUnit.SECONDS);
  }

  private void processScrobbles() {
    lastFmUserService.getUserSession().ifPresent(this::processScrobbles);
  }

  private void processScrobbles(UserSession userSession) {
    if (lastFmUserService.isOnlineScrobblingDisabled()) {
      LOG.info("Online scrobbling is disabled, not processing scrobbles");
      return;
    }
    try {
      var scrobbles = scrobblesDao.getNewestScrobbles();
      if (scrobbles.isEmpty()) {
        LOG.info("no scrobbles to process!");
        return;
      }
      LOG.info("processing scrobbling of {}", scrobbles);
      final long maxTimestamp = scrobbles.stream()
          .map(Scrobble::timestamp)
          .max(Long::compareTo)
          .orElse(0L); //shouldn't it be possible to have none timestamps here
      var response = trackApiService.sendScrobbleTracksRequest(scrobbles, userSession.token());
      LOG.info("response of scrobbling tracks: {} is {}", scrobbles, response);
      if (response instanceof ErrorResponse errorResponse && errorResponse.error() == INVALID_SESSION_KEY) {
        eventBus.post(new InvalidSessionKeyEvent(userSession.username()));
      }
      if (response instanceof SuccessResponse) {
        int rowsAffected = scrobblesDao.deleteByTimestampLessThanEqual(maxTimestamp);
        LOG.info("{} rows deleted from scrobbles outbox table", rowsAffected);
      }
    } catch (Exception e) {
      LOG.warn("error while processing scrobbles: {}", e.getMessage());
    }
  }
}
