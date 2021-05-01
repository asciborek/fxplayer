package com.github.asciborek.player.queue;

import com.github.asciborek.player.Track;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderedPlaylistPreviousTrackSelectorTest {

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
  @DisplayName("it should select the previous track from the list")
  void itShouldSelectPreviousTrackFromTheList() {
    final List<Track> playlist = List.of(firstTrack, secondTrack, thirdTrack, fourthTrack,
        fifthTrack, sixthTrack);
    final PreviousTrackSelector previousTrackSelector = new OrderedPlaylistPreviousTrackSelector(playlist);
    var previousTrack = previousTrackSelector.getPreviousTrack(thirdTrack);
    Assertions.assertEquals(Optional.of(secondTrack), previousTrack);
  }

  @Test
  @DisplayName("it should return the empty optional for the first track")
  void itShouldReturnTheEmptyOptionalForTheFirstTrack() {
    final List<Track> playlist = List.of(firstTrack, secondTrack, thirdTrack, fourthTrack,
        fifthTrack, sixthTrack);
    final PreviousTrackSelector previousTrackSelector = new OrderedPlaylistPreviousTrackSelector(playlist);
    var previousTrack = previousTrackSelector.getPreviousTrack(firstTrack);
    Assertions.assertEquals(Optional.empty(), previousTrack);
  }

  @Test
  @DisplayName("if the playlist is empty then the empty optional should be returned")
  void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
    final PreviousTrackSelector previousTrackSelector = new OrderedPlaylistPreviousTrackSelector(List.of());
    var previousTrack = previousTrackSelector.getPreviousTrack(firstTrack);
    Assertions.assertEquals(Optional.empty(), previousTrack);
  }

  @Test
  @DisplayName("if the playlist does not contain the track then the empty optional should be returned")
  void ifPlaylistDoesNotContainTrackThenEmptyOptionalShouldBeReturned() {
    final PreviousTrackSelector previousTrackSelector = new OrderedPlaylistPreviousTrackSelector(List.of(secondTrack, thirdTrack));
    var previousTrack = previousTrackSelector.getPreviousTrack(fourthTrack);
    Assertions.assertEquals(Optional.empty(), previousTrack);
  }

}
