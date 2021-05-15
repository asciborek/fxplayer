package com.github.asciborek.player.queue;

import com.github.asciborek.playlist.Track;
import java.util.List;
import java.util.Optional;

public final class OrderedPlaylistNextTrackSelector implements NextTrackSelector {

  private final List<Track> playlist;

  public OrderedPlaylistNextTrackSelector(List<Track> playlist) {
    this.playlist = playlist;
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
