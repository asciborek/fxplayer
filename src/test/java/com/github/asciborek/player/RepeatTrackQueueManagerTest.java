package com.github.asciborek.player;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.asciborek.metadata.Track;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepeatTrackQueueManagerTest {
  private static final String ARTIST = "Yes";
  private static final String ALBUM = "The Yes Album";
  private final Track zeroTrack = trackWithTitle("Yours Is no Disgrace");
  private final Track firstTrack = trackWithTitle("Clap");
  private final Track secondTrack = trackWithTitle("Starship Trooper");
  private final Track thirdTrack = trackWithTitle("I've Seen All Good People");

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
    var queue = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack);
    var manager = new RepeatTrackQueueManager(queue);
    var previousTrack = manager.getPreviousTrack(3);
    var nextTrack = manager.getNextTrack(3);
    assertThat(previousTrack).isEqualTo(OptionalInt.of(3));
    assertThat(nextTrack).isEqualTo(OptionalInt.of(3));
  }

  @DisplayName("it should return the empty optional if the a track is not the playlist")
  @Test
  void itShouldReturnAnEmptyOptionalIfTrackIsNotOnPlaylist() {
    var queue = List.of(zeroTrack, firstTrack, secondTrack);
    var manager = new RepeatTrackQueueManager(queue);
    var previousTrack = manager.getPreviousTrack(3);
    var nextTrack = manager.getNextTrack(3);
    assertThat(previousTrack).isEqualTo(OptionalInt.empty());
    assertThat(nextTrack).isEqualTo(OptionalInt.empty());
  }
}
