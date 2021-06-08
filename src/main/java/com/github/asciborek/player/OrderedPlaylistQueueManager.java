package com.github.asciborek.player;

import com.github.asciborek.playlist.Track;
import java.util.List;
import java.util.Optional;

final class OrderedPlaylistQueueManager implements QueueManager {

  private static final int FIRST_ELEMENT_INDEX = 0;
  private final List<Track> playlist;

  public OrderedPlaylistQueueManager(List<Track> playlist) {
    this.playlist = playlist;
  }

  @Override
  public Optional<Track> getPreviousTrack(Track currentTrack) {
    if (!playlist.contains(currentTrack) ) {
      return Optional.empty();
    }
    int currentTrackIndex = playlist.indexOf(currentTrack);
    if (currentTrackIndex == FIRST_ELEMENT_INDEX) {
      return Optional.empty();
    }
    return Optional.ofNullable(playlist.get(currentTrackIndex - 1));
  }

  @Override
  public Optional<Track> getNextTrack(Track currentTrack) {
    if (!playlist.contains(currentTrack)) {
      return Optional.empty();
    }
    int currentTrackIndex = playlist.indexOf(currentTrack);
    if (currentTrackIndex == playlist.size() -1) {
      return Optional.empty();
    }
    return Optional.ofNullable(playlist.get(currentTrackIndex + 1));
  }
}
