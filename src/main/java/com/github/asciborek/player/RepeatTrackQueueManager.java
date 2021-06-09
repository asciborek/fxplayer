package com.github.asciborek.player;

import com.github.asciborek.playlist.Track;
import java.util.List;
import java.util.Optional;

final class RepeatTrackQueueManager  implements QueueManager{

  private final List<Track> playlist;

  RepeatTrackQueueManager(List<Track> playlist) {
    this.playlist = playlist;
  }

  @Override
  public Optional<Track> getPreviousTrack(Track currentTrack) {
    return playlist.contains(currentTrack) ? Optional.of(currentTrack) : Optional.empty();
  }

  @Override
  public Optional<Track> getNextTrack(Track currentTrack) {
    return playlist.contains(currentTrack) ? Optional.of(currentTrack) : Optional.empty();
  }
}
