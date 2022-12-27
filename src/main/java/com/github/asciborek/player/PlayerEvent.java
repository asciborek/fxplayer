package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerEvent.PausePlayingTrackEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistClearedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistOpenedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistShuffledEvent;
import com.github.asciborek.player.PlayerEvent.ResumePlayingTrackEvent;
import com.github.asciborek.player.PlayerEvent.ShowSidebarChangeEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import java.time.Instant;

public sealed interface  PlayerEvent permits PlaylistClearedEvent, PlaylistFinishedEvent,
    PlaylistOpenedEvent, StartPlayingTrackEvent, PausePlayingTrackEvent, ResumePlayingTrackEvent,
    PlaylistShuffledEvent, ShowSidebarChangeEvent {

  record PlaylistClearedEvent() implements PlayerEvent {}

  record PlaylistFinishedEvent() implements PlayerEvent {}

  record PlaylistOpenedEvent() implements PlayerEvent {}

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
