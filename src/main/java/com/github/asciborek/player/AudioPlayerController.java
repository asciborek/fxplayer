package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerCommand.OpenTrackFileCommand;
import com.github.asciborek.player.PlayerCommand.PlayOrPauseTrackCommand;
import com.github.asciborek.player.PlayerCommand.RemoveTrackCommand;
import com.github.asciborek.player.PlayerEvent.PausePlayingTrackEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistClearedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistOpenedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistShuffledEvent;
import com.github.asciborek.player.PlayerEvent.ResumePlayingTrackEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.github.asciborek.player.TracksFilesWatcher.TracksFilesDeletedEvent;
import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.util.DurationUtils;
import com.github.asciborek.util.TimeProvider;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
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

public final class AudioPlayerController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(AudioPlayerController.class);
  private static final int NOT_PLAYING_TRACK_FLAG = -1;

  private final EventBus eventBus;
  private final SettingsService settingsService;
  private final ObservableList<Track> tracksQueue;
  private final TimeProvider timeProvider;

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

  public AudioPlayerController(EventBus eventBus, SettingsService settingsService,
      ObservableList<Track> tracksQueue, TimeProvider timeProvider) {
    this.eventBus = eventBus;
    this.settingsService = settingsService;
    this.tracksQueue = tracksQueue;
    this.timeProvider = timeProvider;
    this.queueManager = new OrderedPlaylistQueueManager(tracksQueue);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    trackProgress.setProgress(0);
    volumeSlider.valueProperty().bindBidirectional(volumeProperty);
    volumeProperty.addListener(this::onVolumeChange);
    volumeProperty.set(settingsService.getVolume());
    tracksQueue.addListener(this::onPlaylistChange);
    var playlistToggleGroup = new ToggleGroup();
    playlistToggleGroup.getToggles().addAll(repeatPlaylistButton, repeatTrackButton);
    playlistToggleGroup.selectedToggleProperty().addListener(this::onPlaylistToggleGroupChanged);
  }

  @Subscribe
  public void onOpenTrackFileCommand(OpenTrackFileCommand openTrackFileCommand) {
    currentTrack = openTrackFileCommand.track();
    currentTrackIndex = 0;
    LOG.info("onOpenTrackFileCommand {}", currentTrack);
    startPlayingNewTrack();
  }

  @Subscribe
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
  public void onPlaylistOpenedEvent(PlaylistOpenedEvent event) {
    if (!tracksQueue.isEmpty()) {
      startPlayingNewTrack(0);
    }
  }

  @Subscribe
  public void onPlaylistShuffled(PlaylistShuffledEvent playlistShuffledEvent) {
    if (currentTrack != null) {
      currentTrackIndex = tracksQueue.indexOf(currentTrack);
      LOG.info("onPlaylistShuffled: a new track index {} for a track: {}", currentTrackIndex, currentTrack);
    }
  }

  @Subscribe
  public void onRemoveTrackCommand(RemoveTrackCommand removeTrackCommand) {
    int removedTrackIndex = removeTrackCommand.trackIndex();
    LOG.info("received RemoveTrackCommand, track index: {}", removedTrackIndex);
    if (removedTrackIndex == currentTrackIndex) {
      removeTheCurrentTrack(removedTrackIndex);
    } else {
      tracksQueue.remove(removedTrackIndex);
      if (removedTrackIndex < currentTrackIndex) {
        currentTrackIndex--;
      }
    }
  }

  @Subscribe
  public void onTracksFilesDeletedEvent(TracksFilesDeletedEvent event) {
    Platform.runLater(() -> {
      var deletedFiles = event.trackPaths();
      tracksQueue.removeIf(track -> deletedFiles.contains(track.filePath()));
    });
  }

  private void removeTheCurrentTrack(int removedTrackIndex) {
    if (currentTrack != null) {
      pauseTrack();
    }
    tracksQueue.remove(removedTrackIndex);
    if (currentTrackIndex < tracksQueue.size() - 1) {
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
    int playlistSize = tracksQueue.size();
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
      queueManager = new RepeatTrackQueueManager(tracksQueue);
    } else if (newSelectedButton == repeatPlaylistButton) {
      queueManager = new RepeatPlaylistQueueManager(tracksQueue);
    } else {
      queueManager = new OrderedPlaylistQueueManager(tracksQueue);
    }
  }

  private void onEmptyPlaylist() {
    playlistTotalTimeLabel.setText("");
    eventBus.post(new PlaylistClearedEvent());
  }

  private void formatPlaylistTotalTimeLabelForSingleTrack() {
    playlistTotalTimeLabel.setText("1 tracks - [" + DurationUtils.format(tracksQueue.get(0).duration()) + "] ");
  }

  private void formatPlaylistTotalTimeLabelForMultiplyTracks(int totalTracks) {
    var totalDuration = getTotalPlaylistDuration();
    var playlistTotalTimeText = " " + totalTracks + " tracks - ["
        + DurationUtils.format(totalDuration) + "] ";
    playlistTotalTimeLabel.setText(playlistTotalTimeText);
  }

  private java.time.Duration getTotalPlaylistDuration() {
    return tracksQueue.stream()
        .map(Track::duration)
        .reduce(java.time.Duration::plus)
        .orElse(java.time.Duration.ZERO);
  }

  public void onPlayOrPauseButtonClicked() {
    if (playerState == PlayerState.READY && !tracksQueue.isEmpty()) {
      currentTrack = tracksQueue.getFirst();
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
    currentTrack = tracksQueue.get(nextTrack);
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
    var trackPlayedEventPublisher = new TrackPlayedEventPublisher(currentTrack, eventBus, timeProvider);
    mediaPlayer = new MediaPlayer(media);
    mediaPlayer.volumeProperty().bind(volumeProperty);
    mediaPlayer.currentTimeProperty().addListener(this::onCurrentTimeListener);
    mediaPlayer.currentTimeProperty().addListener(trackPlayedEventPublisher::onTrackProgress);
    mediaPlayer.play();
    mediaPlayer.setOnPlaying(trackPlayedEventPublisher::onTrackPlaying);
    mediaPlayer.setOnPaused(trackPlayedEventPublisher::onTrackPaused);
    mediaPlayer.setOnEndOfMedia(this::nextTrack);
  }

  private void resumePlayingTrack() {
    mediaPlayer.play();
    playerState = PlayerState.PLAYING;
    eventBus.post(new ResumePlayingTrackEvent(currentTrack));
  }

  private void pauseTrack() {
    mediaPlayer.pause();
    playerState = PlayerState.PAUSED;
    eventBus.post(new PausePlayingTrackEvent(currentTrack));
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
