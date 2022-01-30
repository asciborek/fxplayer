package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerEvent.PlaylistClearedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistOpenedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistShuffledEvent;
import com.github.asciborek.player.PlayerEvent.ShowSidebarChangeEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;

public sealed interface  PlayerEvent permits PlaylistClearedEvent, PlaylistFinishedEvent,
    PlaylistOpenedEvent, StartPlayingTrackEvent, PlaylistShuffledEvent, ShowSidebarChangeEvent {

  record PlaylistClearedEvent() implements PlayerEvent {}

  record PlaylistFinishedEvent() implements PlayerEvent {}

  record PlaylistOpenedEvent() implements PlayerEvent {}

  record StartPlayingTrackEvent(Track track) implements PlayerEvent {}

  record PlaylistShuffledEvent() implements PlayerEvent {}

  record ShowSidebarChangeEvent(boolean showSidebar) implements PlayerEvent {}

}
