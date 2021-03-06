package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import java.util.List;
import java.util.OptionalInt;

final class RepeatPlaylistQueueManager implements QueueManager {

  private static final int FIRST_ELEMENT_INDEX = 0;
  private final List<Track> playlist;

  RepeatPlaylistQueueManager(List<Track> playlist) {
    this.playlist = playlist;
  }

  @Override
  public OptionalInt getPreviousTrack(int currentTrack) {
    if (playlist.isEmpty() || hasInvalidIndex(currentTrack)) {
      return OptionalInt.empty();
    }
    if (currentTrack == FIRST_ELEMENT_INDEX) {
      return OptionalInt.of(playlist.size() - 1);
    }
    return OptionalInt.of(currentTrack - 1);
  }

  @Override
  public OptionalInt getNextTrack(int currentTrack) {
    if (playlist.isEmpty() || hasInvalidIndex(currentTrack)) {
      return OptionalInt.empty();
    }
    if (currentTrack == playlist.size() - 1) {
      return OptionalInt.of(FIRST_ELEMENT_INDEX);
    }
    return OptionalInt.of(currentTrack + 1);
  }

  private boolean hasInvalidIndex(int currentTrack) {
    return currentTrack < FIRST_ELEMENT_INDEX || currentTrack > playlist.size() - 1;
  }

}
