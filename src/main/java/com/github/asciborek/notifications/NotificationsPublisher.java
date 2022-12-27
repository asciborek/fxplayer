package com.github.asciborek.notifications;

import com.github.asciborek.player.PlayerEvent.PausePlayingTrackEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvent.ResumePlayingTrackEvent;
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
  public void onStartPlayingTrackEvent(StartPlayingTrackEvent startPlayingTrackEvent) {
    LOG.info("received startPlayingTrackEvent {}", startPlayingTrackEvent);
    var track = startPlayingTrackEvent.track();
    notificationsFactory
        .startPlayingTrackNotification(track)
        .showInformation();
  }

  @Subscribe
  public void onPausePlayingTrackEvent(PausePlayingTrackEvent pausePlayingTrackEvent) {
    LOG.info("received pausePlayingTrackEvent {}", pausePlayingTrackEvent);
    var track = pausePlayingTrackEvent.track();
    notificationsFactory
        .pausePlayingTrackNotification(track)
        .showInformation();
  }

  @Subscribe
  public void onResumePlayingTrackEvent(ResumePlayingTrackEvent resumePlayingTrackEvent) {
    LOG.info("received pausePlayingTrackEvent {}", resumePlayingTrackEvent);
    var track = resumePlayingTrackEvent.track();
    notificationsFactory
        .resumePlayingTrackNotification(track)
        .showInformation();
  }

  @Subscribe
  public void onPlaylistFinishedEvent(PlaylistFinishedEvent playlistFinishedEvent) {
    LOG.info("received playlistFinishedEvent");
    notificationsFactory
        .playlistFinishedNotification()
        .showInformation();
  }

}
