package com.github.asciborek.player.command;

import com.github.asciborek.player.Track;

public final class OpenTrackFileCommand {

  private final Track track;

  public OpenTrackFileCommand(Track track) {
    this.track = track;
  }

  public Track getTrack() {
    return track;
  }
}
