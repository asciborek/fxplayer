package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import java.io.File;

public sealed interface PlayerCommand {

  record OpenTrackFileCommand(Track track) implements PlayerCommand {}

  record PlayOrPauseTrackCommand(Track track, int trackIndex) implements PlayerCommand {}

  record RemoveTrackCommand(int trackIndex) implements PlayerCommand {}

  record OpenFileCommand(File file) implements PlayerCommand {}

  record AddTrackCommand(File trackFile) implements PlayerCommand {}

  record AddDirectoryCommand(File directory) implements PlayerCommand {}

  record SavePlaylistCommand(File playlistFile) implements PlayerCommand {}

  record LoadPlaylistCommand(File playlistFile) implements PlayerCommand {}

  record ClearPlaylistCommand() implements PlayerCommand {}

  record ShufflePlaylistCommand() implements PlayerCommand {}

}
