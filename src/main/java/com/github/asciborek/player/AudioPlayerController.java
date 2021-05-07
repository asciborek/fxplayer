package com.github.asciborek.player;

import com.github.asciborek.player.command.OpenTrackFileCommand;
import com.github.asciborek.player.command.PlayOrPauseTrackCommand;
import com.github.asciborek.player.event.StartPlayingTrackEvent;
import com.github.asciborek.player.queue.NextTrackSelector;
import com.github.asciborek.player.queue.OrderedPlaylistNextTrackSelector;
import com.github.asciborek.player.queue.OrderedPlaylistPreviousTrackSelector;
import com.github.asciborek.player.queue.PreviousTrackSelector;
import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.util.DurationUtils;
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
public final class AudioPlayerController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(AudioPlayerController.class);

  private final EventBus eventBus;
  private final SettingsService settingsService;
  private final ObservableList<Track> tracks;
  private PlayerState playerState = PlayerState.READY;
  private Track currentTrack;
  private MediaPlayer mediaPlayer;
  private NextTrackSelector nextTrackSelector;
  private PreviousTrackSelector previousTrackSelector;
  private final DoubleProperty volumeProperty = new SimpleDoubleProperty(1);
  // UI Fields
  @FXML
  private Label volumeLabel;
  @FXML
  private Slider volumeSlider;
  @FXML
  private Label currentTimeLabel;
  @FXML
  private Label totalTimeLabel;
  @FXML
  private ProgressBar trackProgress;

  @Inject
  public AudioPlayerController(EventBus eventBus, SettingsService settingsService, ObservableList<Track> tracks) {
    this.eventBus = eventBus;
    this.settingsService = settingsService;
    this.tracks = tracks;
    this.nextTrackSelector = new OrderedPlaylistNextTrackSelector(tracks);
    this.previousTrackSelector = new OrderedPlaylistPreviousTrackSelector(tracks);
    eventBus.register(this);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    trackProgress.setProgress(0);
    volumeSlider.valueProperty().bindBidirectional(volumeProperty);
    volumeProperty.addListener(this::onVolumeChange);
    volumeProperty.set(settingsService.getVolume());
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onOpenTrackFileCommand(OpenTrackFileCommand openTrackFileCommand) {
    currentTrack = openTrackFileCommand.getTrack();
    LOG.info("onOpenTrackFileCommand {}", currentTrack);
    startPlayingNewTrack();
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlayOrPauseCommand(PlayOrPauseTrackCommand playOrPauseTrackCommand) {
    Track newTrack = playOrPauseTrackCommand.getTrack();
    LOG.info("play or pause track {}", newTrack);
    switch (playerState) {
      case READY:
        currentTrack = newTrack;
        startPlayingNewTrack();
        break;
      case PAUSED:
        if (newTrack.equals(currentTrack)) {
          resumePlayingTrack();
        } else {
          currentTrack = newTrack;
          startPlayingNewTrack();
        }
        break;
      case PLAYING:
        if (newTrack.equals(currentTrack)) {
          pauseTrack();
        } else {
          currentTrack = newTrack;
          startPlayingNewTrack();
        }
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
    settingsService.setVolume(newValue.doubleValue());
    LOG.info("the old volume value: {}, the new volume value: {}", oldValue.doubleValue(), newValue.doubleValue());
  }

  private void onCurrentTimeListener(ObservableValue<? extends Duration> observableValue, Duration oldDuration, Duration newDuration) {
    double progress = newDuration.toSeconds()/mediaPlayer.getTotalDuration().toSeconds();
    currentTimeLabel.setText(DurationUtils.formatTimeInSeconds((int)newDuration.toSeconds()));
    trackProgress.setProgress(progress);
  }

  public void onPlayOrPauseButtonClicked() {
    if (playerState == PlayerState.READY && !tracks.isEmpty()) {
      currentTrack = tracks.get(0);
      startPlayingNewTrack();
    } else if(playerState == PlayerState.PAUSED) {
      resumePlayingTrack();
    } else if (playerState == PlayerState.PLAYING) {
      pauseTrack();
    }
  }

  public void onNextTrackButtonClicked() {
    if (playerState != PlayerState.READY) {
      nextTrack();
    }
  }

  public void onPreviousTrackButtonClicked() {
    if (playerState != PlayerState.READY) {
      previousTrackSelector.getPreviousTrack(currentTrack)
          .ifPresent(this::startPlayingNewTrack);
    }
  }

  private void nextTrack() {
    nextTrackSelector.getNextTrack(currentTrack)
        .ifPresentOrElse(this::startPlayingNewTrack, this::onPlaylistFinished);
  }

  private void startPlayingNewTrack(Track nextTrack) {
    currentTrack = nextTrack;
    startPlayingNewTrack();
  }

  private void startPlayingNewTrack() {
    if (playerState != PlayerState.READY) {
      mediaPlayer.stop();
    }
    initMediaPlayerForNewTrack();
    totalTimeLabel.setText(DurationUtils.format(currentTrack.getDuration()));
    playerState = PlayerState.PLAYING;
    eventBus.post(new StartPlayingTrackEvent(currentTrack));
  }

  private void initMediaPlayerForNewTrack() {
    Media media = new Media(currentTrack.getFilePath().toUri().toString());
    mediaPlayer = new MediaPlayer(media);
    mediaPlayer.volumeProperty().bind(volumeProperty);
    mediaPlayer.currentTimeProperty().addListener(this::onCurrentTimeListener);
    mediaPlayer.play();
    mediaPlayer.setOnEndOfMedia(this::nextTrack);
  }

  private void resumePlayingTrack() {
    mediaPlayer.play();
    playerState = PlayerState.PLAYING;
  }

  private void pauseTrack() {
    mediaPlayer.pause();
    playerState = PlayerState.PAUSED;
  }

  private void onPlaylistFinished() {
    trackProgress.setProgress(0);
    totalTimeLabel.setText("");
    currentTimeLabel.setText("");
    LOG.info("playlist finished");
  }

  private enum PlayerState {
    READY, PLAYING, PAUSED
  }

}
