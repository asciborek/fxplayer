package com.github.asciborek.player;

import com.github.asciborek.playlist.Track;
import java.util.List;
import java.util.OptionalInt;

final class RepeatTrackQueueManager  implements QueueManager{

  private final List<Track> playlist;

  RepeatTrackQueueManager(List<Track> playlist) {
    this.playlist = playlist;
  }

  @Override
  public OptionalInt getPreviousTrack(int currentTrack) {
    if (playlist.isEmpty() || currentTrack < 0 || currentTrack >= playlist.size()) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(currentTrack);
  }

  @Override
  public OptionalInt getNextTrack(int currentTrack) {
    return getPreviousTrack(currentTrack);
  }

}
