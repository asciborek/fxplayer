package com.github.asciborek.player;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.asciborek.GenericTestEventListener;
import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerEvent.TrackPlayedEvent;
import com.github.asciborek.util.ConstantTimeProvider;
import com.google.common.eventbus.EventBus;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
final class TrackPlayedEventPublisherTest {

  private final EventBus eventBus = new EventBus();
  private final Instant now = Instant.now();
  private final GenericTestEventListener<TrackPlayedEvent> testEventListener = new GenericTestEventListener<>(TrackPlayedEvent.class);

  @BeforeAll
  void registerTestEventListener() {
    eventBus.register(testEventListener);
  }

  @AfterEach
  void clearTestEventListenerEvents() {
    testEventListener.clearEvents();
  }

  private final int HALF_MINUTE_TO_MILLIS = 30 * 1000;
  private static final Track IN_MEMORIAM = Track.builder()
      .withArtist("Haken")
      .withAlbum("The Mountain")
      .withTitle("In Memoriam")
      .withDuration(Duration.ofSeconds(4* 60 + 42))
      .withFilePath(null)
      .build();


  private static final Track THE_ARCHITECT = Track.builder()
      .withArtist("Haken")
      .withAlbum("Affinity")
      .withTitle("The Architect")
      .withDuration(Duration.ofSeconds(15* 60 + 42))
      .withFilePath(null)
      .build();

  @Test
  @DisplayName("publish event after half of short track")
  void publishEventAfterHalfOfShortTrackWithValidDurationDiffs() {
    //given
    final var eventPublisher = new TrackPlayedEventPublisher(IN_MEMORIAM, eventBus,
        new ConstantTimeProvider(now),
        TrackPlayedEventPublisher.DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT,
        HALF_MINUTE_TO_MILLIS);

    for (int i = 0; i < 5; i++) {
      eventPublisher.onTrackProgress(null, fromMillis(i * HALF_MINUTE_TO_MILLIS),
          fromMillis((i + 1) * HALF_MINUTE_TO_MILLIS));
    }

    //when
    assertThat(testEventListener.getEventsCount()).isEqualTo(1);
    assertThat(testEventListener.getEventsSnapshot()).first()
        .extracting(TrackPlayedEvent::timestamp)
        .isEqualTo(now.toEpochMilli());
  }


  @Test
  @DisplayName("publish event after max threshold for long track")
  void publishEventAfterMaxThresholdForLongTrack() {
    //given
    final var eventPublisher = new TrackPlayedEventPublisher(THE_ARCHITECT, eventBus,
        new ConstantTimeProvider(now),
        TrackPlayedEventPublisher.DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT,
        HALF_MINUTE_TO_MILLIS);

    for (int i = 0; i < 8; i++) {
      eventPublisher.onTrackProgress(null, fromMillis(i * HALF_MINUTE_TO_MILLIS),
          fromMillis((i + 1) * HALF_MINUTE_TO_MILLIS));
    }

    //when
    assertThat(testEventListener.getEventsCount()).isEqualTo(1);
    assertThat(testEventListener.getEventsSnapshot()).first()
        .extracting(TrackPlayedEvent::timestamp)
        .isEqualTo(now.toEpochMilli());
  }


  @Test
  @DisplayName("don't publish event before half of track and event publish threshold")
  void dontPublishEventBeforeHalfOfTrackAndEventPublishThreshold() {
    //given
    final var eventPublisher = new TrackPlayedEventPublisher(IN_MEMORIAM, eventBus,
        new ConstantTimeProvider(now),
        TrackPlayedEventPublisher.DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT,
        HALF_MINUTE_TO_MILLIS);

    for (int i = 0; i < 2; i++) {
      eventPublisher.onTrackProgress(null, fromMillis(i * HALF_MINUTE_TO_MILLIS),
          fromMillis((i + 1) * HALF_MINUTE_TO_MILLIS));
    }

    //when
    assertThat(testEventListener.getEventsCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("don't publish event after half of track if track was paused")
  void dontPublishEventAfterHalfOfTrackIfTrackWasPaused() {
    //given
    final var eventPublisher = new TrackPlayedEventPublisher(IN_MEMORIAM, eventBus,
        new ConstantTimeProvider(now),
        TrackPlayedEventPublisher.DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT,
        HALF_MINUTE_TO_MILLIS);
    eventPublisher.onTrackPaused();

    for (int i = 0; i < 5; i++) {
      eventPublisher.onTrackProgress(null, fromMillis(i * HALF_MINUTE_TO_MILLIS),
          fromMillis((i + 1) * HALF_MINUTE_TO_MILLIS));
    }

    //when
    assertThat(testEventListener.getEventsCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("don't publish event after half of track if duration diffs are to big")
  void dontPublishEventAfterHalfOfTrackIfDurationDiffsAreToBig() {
    //given
    final var eventPublisher = new TrackPlayedEventPublisher(IN_MEMORIAM, eventBus,
        new ConstantTimeProvider(now),
        TrackPlayedEventPublisher.DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT,
        HALF_MINUTE_TO_MILLIS);
    eventPublisher.onTrackPaused();

    for (int i = 0; i < 5; i++) {
      eventPublisher.onTrackProgress(null, fromMillis(i * 2 * HALF_MINUTE_TO_MILLIS),
          fromMillis((i + 1) *  2 * HALF_MINUTE_TO_MILLIS));
    }

    //when
    assertThat(testEventListener.getEventsCount()).isEqualTo(0);
  }


  private javafx.util.Duration fromMillis(int millis) {
    return javafx.util.Duration.millis(millis);
  }

}
