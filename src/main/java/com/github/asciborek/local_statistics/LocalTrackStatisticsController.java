package com.github.asciborek.local_statistics;

import com.github.asciborek.player.PlayerEvent.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.github.asciborek.util.TimeProvider;
import com.google.common.eventbus.Subscribe;
import java.time.Instant;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

final class LocalTrackStatisticsController {

  private static final String EMPTY_DATE = "";

  @FXML
  private TextField totalPlaysCount;

  @FXML
  private TextField firstPlayed;

  @FXML
  private TextField lastPlayed;

  private final TrackLocalStatisticsProvider trackLocalStatisticsProvider;
  private final TimeProvider timeProvider;

  public LocalTrackStatisticsController(TrackLocalStatisticsProvider trackLocalStatisticsProvider, TimeProvider timeProvider) {
    this.trackLocalStatisticsProvider = trackLocalStatisticsProvider;
    this.timeProvider = timeProvider;
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onNewTrack(StartPlayingTrackEvent event) {
    var artist = event.track().artist();
    var title = event.track().title();
    trackLocalStatisticsProvider.getTrackLocalStatistics(artist, title)
        .thenAccept(this::displayStatistics)
        .exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlaylistFinished(PlaylistFinishedEvent event) {
    totalPlaysCount.setText("");
    firstPlayed.setText(EMPTY_DATE);
    lastPlayed.setText(EMPTY_DATE);

  }

  private void displayStatistics(TrackLocalStatistics trackStatistics) {
    var firstPlayed =  trackStatistics.firstPlayed()
        .map(this::formatDate)
        .orElse(EMPTY_DATE);

    var lastPlayed = trackStatistics.lastPlayed()
        .map(this::formatDate)
        .orElse(EMPTY_DATE);

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
        .toString();
  }

}
