package com.github.asciborek.player;

import com.github.asciborek.player.event.PlayOrPauseTrackCommand;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")//Guava EventBus
public class AudioPlayerController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(AudioPlayerController.class);

  private final EventBus eventBus;
  private final ObservableList<Track> tracks;
  private PlayerState playerState = PlayerState.READY;
  private Track currentTrack;
  private MediaPlayer mediaPlayer;
  private final DoubleProperty volumeProperty = new SimpleDoubleProperty(1);
  // UI Fields
  @FXML
  private Label volumeLabel;
  @FXML
  private Slider volumeSlider;
  @FXML
  private ProgressBar trackProgress;

  @Inject
  public AudioPlayerController(EventBus eventBus, ObservableList<Track> tracks) {
    this.eventBus = eventBus;
    this.tracks = tracks;
    eventBus.register(this);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    trackProgress.setProgress(0);
    volumeSlider.valueProperty().bindBidirectional(volumeProperty);
    volumeProperty.addListener(this::onVolumeChange);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlayOrPauseCommand(PlayOrPauseTrackCommand playOrPauseTrackCommand) {
    currentTrack = playOrPauseTrackCommand.getTrack();
    LOG.info("play or pause track {}", currentTrack);
    switch (playerState) {
      case READY:
        startPlayingTrack();
        break;
      case PAUSED:
        resumePlayingTrack();
        break;
      case PLAYING:
        pauseTrack();
        break;
    }
  }

  public void seek(MouseEvent mouseEvent) {
    if (playerState == PlayerState.PAUSED || playerState == PlayerState.PLAYING) {
      double selectedValue = (mouseEvent.getX()/ trackProgress.getWidth());
      double newCurrentTime = selectedValue * mediaPlayer.getTotalDuration().toSeconds();
      LOG.info("the track progress bar clicked, the selected value {}, the new current time: {} (s)", selectedValue, newCurrentTime);
      mediaPlayer.seek(Duration.seconds(newCurrentTime));
    }
  }

  private void onVolumeChange(ObservableValue<? extends Number> property, Number oldValue, Number newValue) {
    volumeLabel.setText(" " + (int)(newValue.doubleValue() * 100) + "%");
    LOG.info("the old volume value: {}, the new volume value: {}", oldValue.doubleValue(), newValue.doubleValue());
  }

  private void onCurrentTimeListener(ObservableValue<? extends Duration> observableValue, Duration oldDuration, Duration newDuration) {
    double progress = newDuration.toSeconds()/mediaPlayer.getTotalDuration().toSeconds();
    trackProgress.setProgress(progress);
  }

  private void startPlayingTrack() {
    Media media = new Media(currentTrack.getFilePath().toUri().toString());
    mediaPlayer = new MediaPlayer(media);
    mediaPlayer.volumeProperty().bind(volumeProperty);
    mediaPlayer.currentTimeProperty().addListener(this::onCurrentTimeListener);
    mediaPlayer.play();
    playerState = PlayerState.PLAYING;
  }

  private void resumePlayingTrack() {
    mediaPlayer.play();
    playerState = PlayerState.PLAYING;
  }

  private void pauseTrack() {
    mediaPlayer.pause();
    playerState = PlayerState.PAUSED;
  }

  private enum PlayerState {
    READY, PLAYING, PAUSED
  }

}
