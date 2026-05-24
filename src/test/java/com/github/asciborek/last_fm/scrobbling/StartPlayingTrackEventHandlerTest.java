package com.github.asciborek.last_fm.scrobbling;


import com.github.asciborek.last_fm.LastFmUserService;
import com.github.asciborek.last_fm.UserSession;
import com.github.asciborek.last_fm.scrobbling.NowPlayingResponse.ErrorResponse;
import com.github.asciborek.last_fm.scrobbling.NowPlayingResponse.SuccessResponse;
import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import java.net.ConnectException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

@TestInstance(Lifecycle.PER_CLASS)
class StartPlayingTrackEventHandlerTest {

  private static final RandomStringUtils randomStringUtils = RandomStringUtils.secure();

  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
  private LastFmUserService lastFmUserService;
  private TrackApiService trackApiService;
  private StartPlayingTrackEventHandler eventHandler;

  @BeforeEach
  void resetMocks() {
    lastFmUserService = Mockito.mock(LastFmUserService.class);
    trackApiService = Mockito.mock(TrackApiService.class);
    eventHandler = eventHandler(Duration.ofMillis(200), Duration.ofSeconds(2));
  }

  @AfterAll
  void closeExecutor() {
    executorService.shutdownNow();
  }

  @Test
  void dontSendRequestIfUserIsNotAuthenticated() {
    Mockito.when(lastFmUserService.getUserSession()).thenReturn(Optional.empty());
    eventHandler.onStartPlayingTrackEvent(event());
    Mockito.verify(trackApiService, Mockito.after(1000).never()).sendUpdateNowPlayingRequest(Mockito.any(), Mockito.any());
  }

  @Test
  void doNotRetryIfFirstAttemptWasSuccessful() {
    UserSession userSession = userSession();
    StartPlayingTrackEvent event = event();
    Mockito.when(lastFmUserService.getUserSession()).thenReturn(Optional.of(userSession));
    Mockito.when(trackApiService.sendUpdateNowPlayingRequest(event.track(), userSession.token())).thenReturn(new SuccessResponse());

    eventHandler.onStartPlayingTrackEvent(event());

    Mockito.verify(trackApiService, Mockito.after(100).times(1))
        .sendUpdateNowPlayingRequest(event.track(), userSession.token());
  }

  @Test
  void retryOnRuntimeException() {
    UserSession userSession = userSession();
    StartPlayingTrackEvent event = event();
    Mockito.when(lastFmUserService.getUserSession()).thenReturn(Optional.of(userSession));
    Mockito.when(trackApiService.sendUpdateNowPlayingRequest(event.track(), userSession.token()))
        .thenThrow(new RuntimeException(new ConnectException()))
        .thenThrow(new RuntimeException(new ConnectException()))
        .thenThrow(new RuntimeException(new ConnectException()))
        .thenThrow(new RuntimeException(new ConnectException()))
        .thenReturn(new SuccessResponse());

    eventHandler.onStartPlayingTrackEvent(event);

    Mockito.verify(trackApiService, Mockito.timeout(2000).times(5))
        .sendUpdateNowPlayingRequest(event.track(), userSession.token());
  }

  @ParameterizedTest
  @EnumSource(value = TrackApiErrorCode.class, names = {"SERVICE_OFFLINE", "TEMPORARY_ERROR"})
  void retryOnTemporaryError(TrackApiErrorCode errorCode) {
    ErrorResponse errorResponse = new ErrorResponse(errorCode, errorCode.getMessage());
    UserSession userSession = userSession();
    StartPlayingTrackEvent event = event();
    Mockito.when(lastFmUserService.getUserSession()).thenReturn(Optional.of(userSession));

    Mockito.when(trackApiService.sendUpdateNowPlayingRequest(event.track(), userSession.token()))
        .thenReturn(errorResponse)
        .thenReturn(errorResponse)
        .thenReturn(errorResponse)
        .thenReturn(errorResponse)
        .thenReturn(new SuccessResponse());

    eventHandler.onStartPlayingTrackEvent(event);

    Mockito.verify(trackApiService, Mockito.timeout(2000).times(5))
        .sendUpdateNowPlayingRequest(event.track(), userSession.token());
  }

  @ParameterizedTest
  @EnumSource(value = TrackApiErrorCode.class, mode = Mode.EXCLUDE, names = {"SERVICE_OFFLINE", "TEMPORARY_ERROR"})
  void doNotRetryOnNotTemporaryError(TrackApiErrorCode errorCode) {
    ErrorResponse errorResponse = new ErrorResponse(errorCode, errorCode.getMessage());
    UserSession userSession = userSession();
    StartPlayingTrackEvent event = event();
    Mockito.when(lastFmUserService.getUserSession()).thenReturn(Optional.of(userSession));

    Mockito.when(trackApiService.sendUpdateNowPlayingRequest(event.track(), userSession.token()))
        .thenReturn(errorResponse)
        .thenReturn(new SuccessResponse());

    eventHandler.onStartPlayingTrackEvent(event);

    Mockito.verify(trackApiService, Mockito.timeout(2000).times(1))
        .sendUpdateNowPlayingRequest(event.track(), userSession.token());
  }

  @Test
  void doNotRetryAfterMaxDuration() {
    StartPlayingTrackEventHandler eventHandler = eventHandler(Duration.ofMillis(200), Duration.ofSeconds(2));
    UserSession userSession = userSession();
    StartPlayingTrackEvent event = event();
    Mockito.when(lastFmUserService.getUserSession()).thenReturn(Optional.of(userSession));
    Mockito.when(trackApiService.sendUpdateNowPlayingRequest(event.track(), userSession.token())).thenAnswer(
        AdditionalAnswers.answersWithDelay(500, _ -> {
          throw new RuntimeException();
        }))
        .thenAnswer(AdditionalAnswers.answersWithDelay(500, _ -> {
          throw new RuntimeException();
        }))
        .thenAnswer(AdditionalAnswers.answersWithDelay(500, _ -> {
          throw new RuntimeException();
        }))
        .thenReturn(new SuccessResponse());

    eventHandler.onStartPlayingTrackEvent(event);

    Mockito.verify(trackApiService, Mockito.after(1500)
        .times(3))
        .sendUpdateNowPlayingRequest(event.track(), userSession.token());
  }

  private StartPlayingTrackEventHandler eventHandler(Duration delay, Duration maxDuration) {
    return new StartPlayingTrackEventHandler(lastFmUserService, trackApiService, executorService, delay, maxDuration);
  }


  private UserSession userSession() {
    return new UserSession(randomStringUtils.next(10, true, false), UUID.randomUUID().toString());
  }

  private StartPlayingTrackEvent event() {
    Track track = Track.builder()
        .withArtist("Haken")
        .withAlbum("Affinity")
        .withTitle("The Architect")
        .build();
    return new StartPlayingTrackEvent(track);
  }
}