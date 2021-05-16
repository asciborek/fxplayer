package com.github.asciborek.player.queue;

import com.github.asciborek.playlist.Track;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderedPlaylistNextTrackSelectorTest {

  private static final String ARTIST = "Yes";
  private static final String ALBUM = "The Yes Album";
  private final Track firstTrack = trackWithTitle("Yours Is no Disgrace");
  private final Track secondTrack = trackWithTitle("Clap");
  private final Track thirdTrack = trackWithTitle("Starship Trooper");
  private final Track fourthTrack = trackWithTitle("I've Seen All Good People");
  private final Track fifthTrack = trackWithTitle("A Venture");
  private final Track sixthTrack = trackWithTitle("Perpetual Change");
  
  private Track trackWithTitle(String title) {
    return Track.builder()
        .withArtist(ARTIST)
        .withAlbum(ALBUM)
        .withTitle(title)
        .withFilePath(Path.of(title + ".mp3"))
        .build();
  }

  @Test
  @DisplayName("it should select the next tracks on the playlist")
  void itShouldSelectNextTrackOnThePlaylist() {
    final List<Track> playlist = List.of(firstTrack, secondTrack, thirdTrack, fourthTrack,
        fifthTrack, sixthTrack);
    final NextTrackSelector nextTrackSelector = new OrderedPlaylistNextTrackSelector(playlist);
    var nextTrack = nextTrackSelector.getNextTrack(secondTrack);
    Assertions.assertEquals(Optional.of(thirdTrack), nextTrack);
  }

  @Test
  @DisplayName("after the last tracks the empty optional should be returned")
  void afterLastTrackEmptyOptionalShouldBeReturned() {
    final List<Track> playlist = List.of(firstTrack, secondTrack, thirdTrack, fourthTrack,
        fifthTrack, sixthTrack);
    final NextTrackSelector nextTrackSelector = new OrderedPlaylistNextTrackSelector(playlist);
    var nextTrack = nextTrackSelector.getNextTrack(sixthTrack);
    Assertions.assertEquals(Optional.empty(), nextTrack);
  }

  @Test
  @DisplayName("if the playlist is empty then the empty optional should be returned")
  void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
    final NextTrackSelector nextTrackSelector = new OrderedPlaylistNextTrackSelector(List.of());
    var nextTrack = nextTrackSelector.getNextTrack(firstTrack);
    Assertions.assertEquals(Optional.empty(), nextTrack);
  }

  @Test
  @DisplayName("if the playlist does not contain the tracks then the empty optional should be returned")
  void ifPlaylistDoesNotContainTrackThenEmptyOptionalShouldBeReturned() {
    final NextTrackSelector nextTrackSelector = new OrderedPlaylistNextTrackSelector(List.of(thirdTrack, fourthTrack));
    var nextTrack = nextTrackSelector.getNextTrack(firstTrack);
    Assertions.assertEquals(Optional.empty(), nextTrack);
  }

}
