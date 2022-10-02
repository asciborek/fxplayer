package com.github.asciborek.notifications;

import com.github.asciborek.player.PlayerEvent.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class NotificationsPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationsPublisher.class);

  private final NotificationsFactory notificationsFactory;

  NotificationsPublisher(NotificationsFactory notificationsFactory) {
    this.notificationsFactory = notificationsFactory;
  }

  @Subscribe
  public void onPlaylistFinished(PlaylistFinishedEvent playlistFinishedEvent) {
    LOG.info("received playlistFinishedEvent");
    notificationsFactory
        .playlistFinishedNotification()
        .showInformation();
  }

  @Subscribe
  public void onStartPlaying(StartPlayingTrackEvent startPlayingTrackEvent) {
    LOG.info("received startPlayingTrackEvent {}", startPlayingTrackEvent);
    var track = startPlayingTrackEvent.track();
    notificationsFactory
        .startPlayingNotification(track)
        .showInformation();
  }

}
