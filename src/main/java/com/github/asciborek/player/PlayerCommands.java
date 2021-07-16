package com.github.asciborek.player;

import com.github.asciborek.playlist.Track;

public final class PlayerCommands {

  private PlayerCommands() {}

  public static record OpenTrackFileCommand(Track track) {}

  public static record PlayOrPauseTrackCommand(Track track, int trackIndex) {}

  public static record RemoveTrackCommand(int trackIndex) {}

}
