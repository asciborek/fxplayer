package com.github.asciborek.player.queue;

import com.github.asciborek.playlist.Track;
import java.util.Optional;

public interface PreviousTrackSelector {

  Optional<Track> getPreviousTrack(Track currentTrack);

}
