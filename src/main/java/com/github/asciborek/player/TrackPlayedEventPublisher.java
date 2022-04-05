package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerEvent.TrackPlayedEvent;
import com.github.asciborek.util.TimeProvider;
import com.google.common.eventbus.EventBus;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TrackPlayedEventPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(TrackPlayedEventPublisher.class);

  static final int DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT = 4 * 60 * 1000;
  static final int DEFAULT_MAX_VALID_MILLIS_DURATION_DELTA = 1000;

  private final Track track;
  private final EventBus eventBus;
  private final TimeProvider timeProvider;
  private final int eventPublishThresholdMillisTime;
  private final int maxMillisDurationDelta;

  private int timeMillisProgress = 0;
  private boolean paused = false;
  private boolean eventAlreadySent = false;

  TrackPlayedEventPublisher(Track track, EventBus eventBus, TimeProvider timeProvider) {
    this(track, eventBus, timeProvider, DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT,
        DEFAULT_MAX_VALID_MILLIS_DURATION_DELTA);
  }

  TrackPlayedEventPublisher(Track track, EventBus eventBus, TimeProvider timeProvider,
      int maxPlayingMillisTimeBeforeSendingEvent, int maxValidMillisDurationDelta) {
    this.track = track;
    this.eventBus = eventBus;
    this.timeProvider = timeProvider;
    this.eventPublishThresholdMillisTime = (int)Math.min(track.duration().toMillis() / 2, maxPlayingMillisTimeBeforeSendingEvent);
    this.maxMillisDurationDelta = maxValidMillisDurationDelta;
  }

  void onTrackProgress(ObservableValue<? extends Duration> i, Duration oldDuration, Duration newDuration) {
    if (eventAlreadySent || paused) {
      return;
    }

    var durationDiff = newDuration.toMillis() - oldDuration.toMillis();
    if (durationDiff > maxMillisDurationDelta) {
      return;
    }

    timeMillisProgress += durationDiff;
    if (timeMillisProgress >= eventPublishThresholdMillisTime) {
     eventAlreadySent = true;
     LOG.info("Sending TrackPlayedEvent for Track {}", track);
     eventBus.post(new TrackPlayedEvent(track, timeProvider.currentTime()));
    }
  }

  void onTrackPaused() {
    paused = true;
  }

  void onTrackPlaying() {
    paused = false;
  }
}
