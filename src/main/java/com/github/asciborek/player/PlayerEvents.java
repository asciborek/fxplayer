package com.github.asciborek.player;

import com.github.asciborek.playlist.Track;

public final class PlayerEvents {

  private PlayerEvents() {}

  public static record PlaylistClearedEvent() {}

  public static record PlaylistFinishedEvent() {}

  public static record PlaylistOpenedEvent() {}

  public static record StartPlayingTrackEvent(Track track) {}

}
