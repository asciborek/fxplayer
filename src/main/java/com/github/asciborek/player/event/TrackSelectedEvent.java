package com.github.asciborek.player.event;

import com.github.asciborek.player.Track;

public class TrackSelectedEvent {

  private final Track track;

  public TrackSelectedEvent(Track track) {
    this.track = track;
  }

  public Track getTrack() {
    return track;
  }
}
