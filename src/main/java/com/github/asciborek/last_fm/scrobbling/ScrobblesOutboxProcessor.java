package com.github.asciborek.last_fm.scrobbling;

import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.last_fm.LastFmUserService;
import com.github.asciborek.last_fm.UserSession;
import com.google.common.eventbus.Subscribe;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrobblesOutboxProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ScrobblesOutboxProcessor.class);

  private final ScheduledExecutorService scheduledExecutorService;
  private final LastFmUserService lastFmUserService;
  private final ScrobblesDao scrobblesDao;
  private final TrackApiService trackApiService;

  private ScheduledFuture<?> processScrobblesFuture;

  public ScrobblesOutboxProcessor(ScheduledExecutorService scheduledExecutorService,
      LastFmUserService lastFmUserService,
      ScrobblesDao scrobblesDao,
      TrackApiService trackApiService) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.lastFmUserService = lastFmUserService;
    this.scrobblesDao = scrobblesDao;
    this.trackApiService = trackApiService;
  }

  public void init() {
    processScrobblesFuture = scheduledExecutorService.scheduleAtFixedRate(this::processScrobbles, 2L, 30L, TimeUnit.SECONDS);
  }

  @Subscribe
  public void cancelTaskOnCloseApplicationEvent(CloseApplicationEvent event) {
    LOG.info("received CloseApplicationEvent, cancelling processScrobblesFuture");
    if (processScrobblesFuture != null) {
      processScrobblesFuture.cancel(false);
    }
  }

  public void processScrobbles() {
    lastFmUserService.getUserSession().ifPresent(this::processScrobbles);
  }

  private void processScrobbles(UserSession userSession) {
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
      if (response instanceof ScrobbleResponse.SuccessResponse) {
        int rowsAffected = scrobblesDao.deleteByTimestampLessThanEqual(maxTimestamp);
        LOG.info("{} rows deleted from scrobbles outbox table", rowsAffected);
      }
    } catch (Exception e) {
      LOG.warn("error while processing scrobbles: {}", e.getMessage());
    }
  }
}
