package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerEvent.TrackPlayedEvent;
import com.github.asciborek.util.ConstantTimeProvider;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TrackPlayedEventPublisherTest {

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
  void shouldPublishEventAfterHalfOfShortTrackWithValidDurationDiffs() {
    //given
    final var eventBus = new EventBus();
    final var now = Instant.now();
    final var mockListener = new MockListener();
    eventBus.register(mockListener);

    final var eventPublisher = new TrackPlayedEventPublisher(IN_MEMORIAM, eventBus,
        new ConstantTimeProvider(now),
        TrackPlayedEventPublisher.DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT,
        HALF_MINUTE_TO_MILLIS);

    for (int i = 0; i < 5; i++) {
      eventPublisher.onTrackProgress(null, fromMillis(i * HALF_MINUTE_TO_MILLIS),
          fromMillis((i + 1) * HALF_MINUTE_TO_MILLIS));
    }

    //when
    Assertions.assertThat(mockListener.eventReceivedCount()).isEqualTo(1);
    Assertions.assertThat(mockListener.event()).isNotEmpty();
    Assertions.assertThat(mockListener.event()).get()
        .extracting(TrackPlayedEvent::timestamp)
        .isEqualTo(now.toEpochMilli());
  }


  @Test
  void shouldPublishEventAfterMaxThresholdForLongTrackWithValidDurationDiffs() {
    //given
    final var eventBus = new EventBus();
    final var now = Instant.now();
    final var mockListener = new MockListener();
    eventBus.register(mockListener);

    final var eventPublisher = new TrackPlayedEventPublisher(THE_ARCHITECT, eventBus,
        new ConstantTimeProvider(now),
        TrackPlayedEventPublisher.DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT,
        HALF_MINUTE_TO_MILLIS);

    for (int i = 0; i < 8; i++) {
      eventPublisher.onTrackProgress(null, fromMillis(i * HALF_MINUTE_TO_MILLIS),
          fromMillis((i + 1) * HALF_MINUTE_TO_MILLIS));
    }

    //when
    Assertions.assertThat(mockListener.eventReceivedCount()).isEqualTo(1);
    Assertions.assertThat(mockListener.event()).isNotEmpty();
    Assertions.assertThat(mockListener.event()).get()
        .extracting(TrackPlayedEvent::timestamp)
        .isEqualTo(now.toEpochMilli());
  }


  @Test
  void shouldNotPublishEventBeforeHalfOfTrackAndEventPublishThresholdForValidDurationDiffs() {
    //given
    final var eventBus = new EventBus();
    final var now = Instant.now();
    final var mockListener = new MockListener();
    eventBus.register(mockListener);

    final var eventPublisher = new TrackPlayedEventPublisher(IN_MEMORIAM, eventBus,
        new ConstantTimeProvider(now),
        TrackPlayedEventPublisher.DEFAULT_MAX_PLAYING_MILLIS_TIME_BEFORE_SENDING_EVENT,
        HALF_MINUTE_TO_MILLIS);

    for (int i = 0; i < 2; i++) {
      eventPublisher.onTrackProgress(null, fromMillis(i * HALF_MINUTE_TO_MILLIS),
          fromMillis((i + 1) * HALF_MINUTE_TO_MILLIS));
    }

    //when
    Assertions.assertThat(mockListener.eventReceivedCount()).isEqualTo(0);
    Assertions.assertThat(mockListener.event()).isEmpty();
  }

  @Test
  void shouldNotPublishEventAfterHalfOfTrackIfTrackWasPaused() {
    //given
    final var eventBus = new EventBus();
    final var now = Instant.now();
    final var mockListener = new MockListener();
    eventBus.register(mockListener);

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
    Assertions.assertThat(mockListener.eventReceivedCount()).isEqualTo(0);
    Assertions.assertThat(mockListener.event()).isEmpty();
  }

  @Test
  void shouldNotPublishEventAfterHalfOfTrackIfDurationDiffsAreToBig() {
    //given
    final var eventBus = new EventBus();
    final var now = Instant.now();
    final var mockListener = new MockListener();
    eventBus.register(mockListener);

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
    Assertions.assertThat(mockListener.eventReceivedCount()).isEqualTo(0);
    Assertions.assertThat(mockListener.event()).isEmpty();
  }


  private javafx.util.Duration fromMillis(int millis) {
    return javafx.util.Duration.millis(millis);
  }

  private static class MockListener {
    private int eventReceivedCount = 0 ;
    private TrackPlayedEvent event;

    @Subscribe
    public void onTrackPlayedEvent(TrackPlayedEvent event) {
      eventReceivedCount++;
      this.event = event;
    }

    public int eventReceivedCount() {
      return eventReceivedCount;
    }

    public Optional<TrackPlayedEvent> event() {
      return Optional.ofNullable(event);
    };
  }

}
