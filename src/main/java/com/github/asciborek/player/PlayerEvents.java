package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;

public final class PlayerEvents {

  private PlayerEvents() {}

  public static record PlaylistClearedEvent() {}

  public static record PlaylistFinishedEvent() {}

  public static record PlaylistOpenedEvent() {}

  public static record StartPlayingTrackEvent(Track track) {}

  static record PlaylistShuffledEvent() {}

  public static record ShowSidebarChangeEvent(boolean showSidebar) {}

}
