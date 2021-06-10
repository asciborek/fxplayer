package com.github.asciborek.player;

import com.github.asciborek.playlist.Track;
import java.util.Optional;

interface QueueManager {

  Optional<Track> getPreviousTrack(Track currentTrack);

  Optional<Track> getNextTrack(Track currentTrack);

}
