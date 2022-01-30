package com.github.asciborek;

import com.github.asciborek.player.PlayerEvent.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.google.common.eventbus.Subscribe;
import javafx.stage.Stage;

@SuppressWarnings("UnstableApiUsage")
public final class WindowTitleUpdater {

  public static final String DEFAULT_TITLE = "FxPlayer";

  private final Stage stage;

  public WindowTitleUpdater(Stage stage) {
    this.stage = stage;
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void updateTitleOnStartPlayingTrackEvent(StartPlayingTrackEvent event) {
    String artist = event.track().artist();
    String title = event.track().title();
    stage.setTitle(artist + " - " + title);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void setDefaultTitleOnPlaylistFinished(PlaylistFinishedEvent event) {
    stage.setTitle(DEFAULT_TITLE);
  }
}
