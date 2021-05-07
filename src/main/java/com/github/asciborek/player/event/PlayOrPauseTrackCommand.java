package com.github.asciborek.player.event;

import com.github.asciborek.player.Track;

public final class PlayOrPauseTrackCommand {

  private final Track track;

  public PlayOrPauseTrackCommand(Track track) {
    this.track = track;
  }

  public Track getTrack() {
    return track;
  }
}
