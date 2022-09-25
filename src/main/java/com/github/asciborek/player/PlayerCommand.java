package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerCommand.AddDirectoryCommand;
import com.github.asciborek.player.PlayerCommand.AddTrackCommand;
import com.github.asciborek.player.PlayerCommand.ClearPlaylistCommand;
import com.github.asciborek.player.PlayerCommand.LoadPlaylistCommand;
import com.github.asciborek.player.PlayerCommand.OpenFileCommand;
import com.github.asciborek.player.PlayerCommand.OpenTrackFileCommand;
import com.github.asciborek.player.PlayerCommand.PlayOrPauseTrackCommand;
import com.github.asciborek.player.PlayerCommand.RemoveTrackCommand;
import com.github.asciborek.player.PlayerCommand.SavePlaylistCommand;
import com.github.asciborek.player.PlayerCommand.ShufflePlaylistCommand;
import java.io.File;

public sealed interface PlayerCommand permits OpenTrackFileCommand, PlayOrPauseTrackCommand,
    RemoveTrackCommand, OpenFileCommand, AddTrackCommand, AddDirectoryCommand, LoadPlaylistCommand,
    SavePlaylistCommand, ClearPlaylistCommand, ShufflePlaylistCommand {

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
