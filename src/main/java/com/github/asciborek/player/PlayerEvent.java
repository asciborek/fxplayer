package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;

import java.time.Instant;
import java.util.Collection;

public sealed interface PlayerEvent {

  record PlaylistClearedEvent() implements PlayerEvent {}

  record PlaylistFinishedEvent() implements PlayerEvent {}

  record PlaylistOpenedEvent() implements PlayerEvent {}

  record TrackAddedEvent(Track track) implements PlayerEvent {}

  record TracksAddedEvent(Collection<Track> tracks) implements PlayerEvent {}

  record StartPlayingTrackEvent(Track track) implements PlayerEvent {}

  record PausePlayingTrackEvent(Track track) implements PlayerEvent {}

  record ResumePlayingTrackEvent(Track track) implements PlayerEvent {}

  record PlaylistShuffledEvent() implements PlayerEvent {}

  record ShowSidebarChangeEvent(boolean showSidebar) implements PlayerEvent {}

  record TrackPlayedEvent(Track track, Instant eventTime) {
    public long timestamp() {
      return eventTime.toEpochMilli();
    }
  }

}
