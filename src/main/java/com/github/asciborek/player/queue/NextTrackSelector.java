package com.github.asciborek.player.queue;

import com.github.asciborek.player.Track;
import java.util.Optional;

public interface NextTrackSelector {

  Optional<Track> getNextTrack(Track currentTrack);

}
