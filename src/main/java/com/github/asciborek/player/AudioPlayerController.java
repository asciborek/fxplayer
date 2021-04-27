package com.github.asciborek.player;

import com.github.asciborek.player.event.TrackSelectedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
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
  }

  @Subscribe
  public void onTrackSelected(TrackSelectedEvent trackSelectedEvent) {
    currentTrack = trackSelectedEvent.getTrack();
    LOG.info("track selected {}", currentTrack);
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

  public void changeVolume() {
    volumeLabel.setText(" " + (int)volumeSlider.getValue() + "%");
    LOG.info("the slider value: {}", volumeSlider.getValue());
  }

  public void seek(MouseEvent mouseEvent) {
    double selectedValue = (mouseEvent.getX()/ trackProgress.getWidth());
    LOG.info("the track progress bar clicked, the selected value {}", selectedValue);
  }

  private void startPlayingTrack() {
    Media media = new Media(currentTrack.getFilePath().toUri().toString());
    mediaPlayer = new MediaPlayer(media);
    mediaPlayer.play();
    playerState = PlayerState.PLAYING;
  }

  private void resumePlayingTrack() {
    mediaPlayer.play();
    playerState = PlayerState.PLAYING;
  }

  private void pauseTrack() {
    mediaPlayer.stop();
    playerState = PlayerState.PAUSED;
  }

  private enum PlayerState {
    READY, PLAYING, PAUSED
  }

}
