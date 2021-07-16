package com.github.asciborek.player;

import java.util.OptionalInt;

interface QueueManager {

  OptionalInt getPreviousTrack(int currentTrack);

  OptionalInt getNextTrack(int currentTrack);

}
