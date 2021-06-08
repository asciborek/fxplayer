package com.github.asciborek.player;

import com.github.asciborek.playlist.Track;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepeatTrackQueueManagerTest {
  private static final String ARTIST = "Yes";
  private static final String ALBUM = "The Yes Album";
  private final Track firstTrack = trackWithTitle("Yours Is no Disgrace");
  private final Track secondTrack = trackWithTitle("Clap");
  private final Track thirdTrack = trackWithTitle("Starship Trooper");
  private final Track fourthTrack = trackWithTitle("I've Seen All Good People");

  private Track trackWithTitle(String title) {
    return Track.builder()
        .withArtist(ARTIST)
        .withAlbum(ALBUM)
        .withTitle(title)
        .withFilePath(Path.of(title + ".mp3"))
        .build();
  }

  @DisplayName("it should return the same track if it is on the playlist")
  @Test
  void itShouldSelectTheSameTrackIfItIsOnPlaylist() {
    var queue = List.of(firstTrack, secondTrack, thirdTrack);
    var manager = new RepeatTrackQueueManager(queue);
    Track previousTrack = manager.getPreviousTrack(secondTrack).get();
    Track nextTrack = manager.getNextTrack(secondTrack).get();
    Assertions.assertEquals(secondTrack, previousTrack);
    Assertions.assertEquals(secondTrack, nextTrack);
  }

  @DisplayName("it should return the empty optional if the a track is not the playlist")
  @Test
  void itShouldReturnAnEmptyOptionalIfTrackIsNotOnPlaylist() {
    var queue = List.of(firstTrack, secondTrack, thirdTrack);
    var manager = new RepeatTrackQueueManager(queue);
    var previousTrack = manager.getPreviousTrack(fourthTrack);
    var nextTrack = manager.getNextTrack(fourthTrack);
    Assertions.assertEquals(Optional.empty(), previousTrack);
    Assertions.assertEquals(Optional.empty(), nextTrack);
  }
}
