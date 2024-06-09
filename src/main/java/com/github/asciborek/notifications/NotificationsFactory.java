package com.github.asciborek.notifications;

import com.github.asciborek.metadata.Track;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

final class NotificationsFactory {

  private static final String NOTIFICATION_TITLE = "FxPlayer";
  private static final Duration START_PLAYING_TRACK_NOTIFICATION_DURATION = Duration.seconds(3);
  private static final Duration PAUSE_PLAYING_TRACK_NOTIFICATION_DURATION = Duration.seconds(5);
  private static final Duration RESUME_PLAYING_TRACK_NOTIFICATION_DURATION = Duration.seconds(3);
  private static final Duration PLAYLIST_FINISHED_NOTIFICATION_DURATION = Duration.seconds(5);

  public Notifications startPlayingTrackNotification(Track track) {
    var text = String.format("Start playing %s - %s", track.artist(), track.title());
    return notification(text, START_PLAYING_TRACK_NOTIFICATION_DURATION);
  }

  public Notifications pausePlayingTrackNotification(Track track) {
    var text = String.format("Pause playing %s - %s", track.artist(), track.title());
    return notification(text, PAUSE_PLAYING_TRACK_NOTIFICATION_DURATION);
  }

  public Notifications resumePlayingTrackNotification(Track track) {
    var text = String.format("Resume playing %s - %s", track.artist(), track.title());
    return notification(text, RESUME_PLAYING_TRACK_NOTIFICATION_DURATION);
  }

  public Notifications playlistFinishedNotification() {
    return notification("Playlist Finished", PLAYLIST_FINISHED_NOTIFICATION_DURATION);
  }

  private Notifications notification(String text, Duration hideAfter) {
    return Notifications.create()
        .title(NOTIFICATION_TITLE)
        .text(text)
        .hideAfter(hideAfter)
        .position(Pos.BOTTOM_RIGHT);
  }

}
