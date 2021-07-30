package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import java.util.List;
import java.util.OptionalInt;

final class OrderedPlaylistQueueManager implements QueueManager {

  private static final int FIRST_ELEMENT_INDEX = 0;
  private final List<Track> playlist;

  OrderedPlaylistQueueManager(List<Track> playlist) {
    this.playlist = playlist;
  }

  @Override
  public OptionalInt getPreviousTrack(int currentTrack) {
    if (playlist.isEmpty()) {
      return OptionalInt.empty();
    }
    if (currentTrack <= 0 || currentTrack > playlist.size() - 1) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(currentTrack - 1);
  }

  @Override
  public OptionalInt getNextTrack(int currentTrack) {
    if (playlist.isEmpty()) {
      return OptionalInt.empty();
    }
    if (currentTrack < 0 || currentTrack >= playlist.size() - 1) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(currentTrack + 1);
  }

}
