package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerCommand.OpenTrackFileCommand;
import com.github.asciborek.player.PlayerCommand.PlayOrPauseTrackCommand;
import com.github.asciborek.player.PlayerCommand.RemoveTrackCommand;

public sealed interface PlayerCommand permits OpenTrackFileCommand, PlayOrPauseTrackCommand,
    RemoveTrackCommand {

  record OpenTrackFileCommand(Track track) implements PlayerCommand {}

  record PlayOrPauseTrackCommand(Track track, int trackIndex) implements PlayerCommand {}

  record RemoveTrackCommand(int trackIndex) implements PlayerCommand {}

}
