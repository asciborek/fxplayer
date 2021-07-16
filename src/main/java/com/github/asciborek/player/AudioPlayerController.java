package com.github.asciborek.player;

import com.github.asciborek.player.PlayerCommands.OpenTrackFileCommand;
import com.github.asciborek.player.PlayerCommands.PlayOrPauseTrackCommand;
import com.github.asciborek.player.PlayerCommands.RemoveTrackCommand;
import com.github.asciborek.player.PlayerEvents.PlaylistClearedEvent;
import com.github.asciborek.player.PlayerEvents.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvents.PlaylistOpenedEvent;
import com.github.asciborek.player.PlayerEvents.PlaylistShuffledEvent;
import com.github.asciborek.player.PlayerEvents.StartPlayingTrackEvent;
import com.github.asciborek.playlist.Track;
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
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")//Guava EventBus
public final class AudioPlayerController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(AudioPlayerController.class);
  private static final int NOT_PLAYING_TRACK_FLAG = -1;

  private final EventBus eventBus;
  private final SettingsService settingsService;
  private final ObservableList<Track> tracks;
  private PlayerState playerState = PlayerState.READY;
  private Track currentTrack;
  private int currentTrackIndex = NOT_PLAYING_TRACK_FLAG;
  private MediaPlayer mediaPlayer;
  private QueueManager queueManager;
  private final DoubleProperty volumeProperty = new SimpleDoubleProperty(1);
  // UI Fields
  @FXML
  private Label volumeLabel;
  @FXML
  private Slider volumeSlider;
  @FXML
  private Label playlistTotalTimeLabel;
  @FXML
  private Label currentTimeLabel;
  @FXML
  private Label totalTimeLabel;
  @FXML
  private ProgressBar trackProgress;

  @FXML
  private ToggleButton repeatPlaylistButton;
  @FXML
  private ToggleButton repeatTrackButton;

  @Inject
  public AudioPlayerController(EventBus eventBus, SettingsService settingsService, ObservableList<Track> tracks) {
    this.eventBus = eventBus;
    this.settingsService = settingsService;
    this.tracks = tracks;
    this.queueManager = new OrderedPlaylistQueueManager(tracks);
    eventBus.register(this);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    trackProgress.setProgress(0);
    volumeSlider.valueProperty().bindBidirectional(volumeProperty);
    volumeProperty.addListener(this::onVolumeChange);
    volumeProperty.set(settingsService.getVolume());
    tracks.addListener(this::onPlaylistChange);
    var playlistToggleGroup = new ToggleGroup();
    playlistToggleGroup.getToggles().addAll(repeatPlaylistButton, repeatTrackButton);
    playlistToggleGroup.selectedToggleProperty().addListener(this::onPlaylistToggleGroupChanged);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onOpenTrackFileCommand(OpenTrackFileCommand openTrackFileCommand) {
    currentTrack = openTrackFileCommand.track();
    currentTrackIndex = 0;
    LOG.info("onOpenTrackFileCommand {}", currentTrack);
    startPlayingNewTrack();
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlayOrPauseCommand(PlayOrPauseTrackCommand playOrPauseTrackCommand) {
    Track newTrack = playOrPauseTrackCommand.track();
    int newTrackIndex = playOrPauseTrackCommand.trackIndex();
    LOG.info("play or pause  track {}, index: {}", newTrack, newTrackIndex);
    switch (playerState) {
      case READY -> {
        currentTrack = newTrack;
        currentTrackIndex = newTrackIndex;
        startPlayingNewTrack();
      }
      case PAUSED -> {
        if (newTrackIndex == currentTrackIndex) {
          resumePlayingTrack();
        } else {
          currentTrack = newTrack;
          currentTrackIndex = newTrackIndex;
          startPlayingNewTrack();
        }
      }
      case PLAYING -> {
        if (newTrackIndex == currentTrackIndex) {
          pauseTrack();
        } else {
          currentTrack = newTrack;
          currentTrackIndex = newTrackIndex;
          startPlayingNewTrack();
        }
      }
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlaylistOpenedEvent(PlaylistOpenedEvent event) {
    if (!tracks.isEmpty()) {
      startPlayingNewTrack(0);
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlaylistShuffled(PlaylistShuffledEvent playlistShuffledEvent) {
    if (currentTrack != null) {
      currentTrackIndex = tracks.indexOf(currentTrack);
      LOG.info("onPlaylistShuffled: a new track index {} for a track: {}", currentTrackIndex, currentTrack);
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onRemoveTrackCommand(RemoveTrackCommand removeTrackCommand) {
    int removedTrackIndex = removeTrackCommand.trackIndex();
    LOG.info("received RemoveTrackCommand, track index: {}", removedTrackIndex);
    if (removedTrackIndex == currentTrackIndex) {
      removeTheCurrentTrack(removedTrackIndex);
    } else {
      tracks.remove(removedTrackIndex);
      if (removedTrackIndex < currentTrackIndex) {
        currentTrackIndex--;
      }
    }
  }

  private void removeTheCurrentTrack(int removedTrackIndex) {
    if (currentTrack != null) {
      pauseTrack();
    }
    tracks.remove(removedTrackIndex);
    if (currentTrackIndex < tracks.size() - 1) {
      startPlayingNewTrack(currentTrackIndex);
    } else {
      onPlaylistFinished();
    }
  }

  public void seek(MouseEvent mouseEvent) {
    if (playerState == PlayerState.PAUSED || playerState == PlayerState.PLAYING) {
      double selectedValue = (mouseEvent.getX()/ trackProgress.getWidth());
      double newCurrentTime = selectedValue * mediaPlayer.getTotalDuration().toSeconds();
      LOG.info("the tracks progress bar clicked, the selected value {}, the new current time: {} (s)", selectedValue, newCurrentTime);
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

  private void onPlaylistChange(Change<? extends Track> change) {
    int playlistSize = tracks.size();
    switch (playlistSize) {
      case 0 -> onEmptyPlaylist();
      case 1 -> formatPlaylistTotalTimeLabelForSingleTrack();
      default -> formatPlaylistTotalTimeLabelForMultiplyTracks(playlistSize);
    }
  }

  private void onPlaylistToggleGroupChanged(ObservableValue<? extends Toggle> change,
      Toggle oldSelectedButton, Toggle newSelectedButton) {
    LOG.info("onPlaylistToggleGroupChanged newSelectedButton: {}", newSelectedButton);
    if (newSelectedButton == repeatTrackButton) {
      queueManager = new RepeatTrackQueueManager(tracks);
    } else if (newSelectedButton == repeatPlaylistButton) {
      queueManager = new RepeatPlaylistQueueManager(tracks);
    } else {
      queueManager = new OrderedPlaylistQueueManager(tracks);
    }
  }

  private void onEmptyPlaylist() {
    playlistTotalTimeLabel.setText("");
    eventBus.post(new PlaylistClearedEvent());
  }

  private void formatPlaylistTotalTimeLabelForSingleTrack() {
    playlistTotalTimeLabel.setText("1 tracks - [" + DurationUtils.format(tracks.get(0).duration()) + "] ");
  }

  private void formatPlaylistTotalTimeLabelForMultiplyTracks(int totalTracks) {
    var totalDuration = getTotalPlaylistDuration();
    var playlistTotalTimeText = " " + totalTracks + " tracks - ["
        + DurationUtils.format(totalDuration) + "] ";
    playlistTotalTimeLabel.setText(playlistTotalTimeText);
  }

  private java.time.Duration getTotalPlaylistDuration() {
    return tracks.stream()
        .map(Track::duration)
        .reduce(java.time.Duration::plus)
        .orElse(java.time.Duration.ZERO);
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
      queueManager.getPreviousTrack(currentTrackIndex)
          .ifPresent(this::startPlayingNewTrack);
    }
  }

  private void nextTrack() {
    queueManager.getNextTrack(currentTrackIndex)
        .ifPresentOrElse(this::startPlayingNewTrack, this::onPlaylistFinished);
  }

  private void startPlayingNewTrack(int nextTrack) {
    currentTrackIndex = nextTrack;
    currentTrack = tracks.get(nextTrack);
    startPlayingNewTrack();
  }

  private void startPlayingNewTrack() {
    if (playerState != PlayerState.READY) {
      mediaPlayer.stop();
    }
    initMediaPlayerForNewTrack();
    totalTimeLabel.setText(DurationUtils.format(currentTrack.duration()));
    playerState = PlayerState.PLAYING;
    eventBus.post(new StartPlayingTrackEvent(currentTrack));
  }

  private void initMediaPlayerForNewTrack() {
    Media media = new Media(currentTrack.filePath().toUri().toString());
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
    eventBus.post(new PlaylistFinishedEvent());
  }

  private enum PlayerState {
    READY, PLAYING, PAUSED
  }

}
