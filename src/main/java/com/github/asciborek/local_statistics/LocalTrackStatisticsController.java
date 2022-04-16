package com.github.asciborek.local_statistics;

import com.github.asciborek.player.PlayerEvent.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.github.asciborek.util.TimeProvider;
import com.google.common.eventbus.Subscribe;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

final class LocalTrackStatisticsController {

  private static final String N_A_DATE = "N/A";
  private static final String EMPTY_STRING = "";

  @FXML
  private TextField totalPlaysCount;

  @FXML
  private TextField firstPlayed;

  @FXML
  private TextField lastPlayed;

  private final TrackLocalStatisticsProvider trackLocalStatisticsProvider;
  private final TimeProvider timeProvider;
  private final DateTimeFormatter dateTimeFormatter;

  public LocalTrackStatisticsController(TrackLocalStatisticsProvider trackLocalStatisticsProvider, TimeProvider timeProvider, DateTimeFormatter dateTimeFormatter) {
    this.trackLocalStatisticsProvider = trackLocalStatisticsProvider;
    this.timeProvider = timeProvider;
    this.dateTimeFormatter = dateTimeFormatter;
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onNewTrack(StartPlayingTrackEvent event) {
    var artist = event.track().artist();
    var title = event.track().title();
    trackLocalStatisticsProvider.getTrackLocalStatistics(artist, title)
        .thenAccept(this::displayStatistics);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlaylistFinished(PlaylistFinishedEvent event) {
    totalPlaysCount.setText(EMPTY_STRING);
    firstPlayed.setText(EMPTY_STRING);
    lastPlayed.setText(EMPTY_STRING);
  }

  private void displayStatistics(TrackLocalStatistics trackStatistics) {
    var firstPlayed =  trackStatistics.firstPlayed()
        .map(this::formatDate)
        .orElse(N_A_DATE);

    var lastPlayed = trackStatistics.lastPlayed()
        .map(this::formatDate)
        .orElse(N_A_DATE);

    displayStatistics(trackStatistics.totalCount(), firstPlayed, lastPlayed);
  }

  private void displayStatistics(long totalCount, String firstPlayedTime, String lastPlayedTime) {
    Platform.runLater(() -> {
      totalPlaysCount.setText(String.valueOf(totalCount));
      firstPlayed.setText(firstPlayedTime);
      lastPlayed.setText(lastPlayedTime);
    });
  }

  private String formatDate(Instant instant) {
    return instant.atZone(timeProvider.applicationZoneId())
        .toLocalDateTime()
        .format(dateTimeFormatter);
  }

}
