package com.github.asciborek.notifications;

import com.github.asciborek.metadata.Track;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

final class NotificationsFactory {

  private static final String NOTIFICATION_TITLE = "FxPlayer";
  private static final Duration PLAYLIST_FINISHED_NOTIFICATION_DURATION = Duration.seconds(5);
  private static final Duration START_PLAYING_TRACK_NOTIFICATION_DURATION = Duration.seconds(3);

  public Notifications playlistFinishedNotification() {
    return Notifications.create()
        .title(NOTIFICATION_TITLE)
        .text("Playlist Finished")
        .hideAfter(PLAYLIST_FINISHED_NOTIFICATION_DURATION)
        .position(Pos.BOTTOM_RIGHT);
  }

  public Notifications startPlayingNotification(Track track) {
    return Notifications.create()
        .title(NOTIFICATION_TITLE)
        .text(String.format("Start playing %s - %s", track.artist(), track.title()))
        .hideAfter(START_PLAYING_TRACK_NOTIFICATION_DURATION)
        .position(Pos.BOTTOM_RIGHT);
  }

}
