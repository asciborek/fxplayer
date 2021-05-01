package com.github.asciborek.player.event;

import com.github.asciborek.player.Track;

public final class StartPlayingTrackEvent {

  private final Track track;

  public StartPlayingTrackEvent(Track track) {
    this.track = track;
  }

  public Track getTrack() {
    return track;
  }
}
