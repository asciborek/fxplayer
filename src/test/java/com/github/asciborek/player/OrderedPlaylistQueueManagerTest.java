package com.github.asciborek.player;

import com.github.asciborek.playlist.Track;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderedPlaylistQueueManagerTest {

  private static final String ARTIST = "Yes";
  private static final String ALBUM = "The Yes Album";
  private final Track zeroTrack = trackWithTitle("Yours Is no Disgrace");
  private final Track firstTrack = trackWithTitle("Clap");
  private final Track secondTrack = trackWithTitle("Starship Trooper");
  private final Track thirdTrack = trackWithTitle("I've Seen All Good People");
  private final Track fourthTrack = trackWithTitle("A Venture");
  private final Track fifthTrack = trackWithTitle("Perpetual Change");

  private Track trackWithTitle(String title) {
    return Track.builder()
        .withArtist(ARTIST)
        .withAlbum(ALBUM)
        .withTitle(title)
        .withFilePath(Path.of(title + ".mp3"))
        .build();
  }

  @Nested
  class GetPreviousTrack {
    @Test
    @DisplayName("it should select the previous tracks from the list")
    void itShouldSelectPreviousTrackFromTheList() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack,
          fourthTrack, fifthTrack);
      final QueueManager queueManager = new OrderedPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(2);
      Assertions.assertEquals(OptionalInt.of(1), previousTrack);
    }

    @Test
    @DisplayName("it should return the empty optional for the first tracks")
    void itShouldReturnTheEmptyOptionalForTheFirstTrack() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack,
          fourthTrack, fifthTrack);
      final QueueManager queueManager = new OrderedPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(0);
      Assertions.assertEquals(OptionalInt.empty(), previousTrack);
    }

    @Test
    @DisplayName("if the playlist is empty then the empty optional should be returned")
    void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of());
      var previousTrack = queueManager.getPreviousTrack(0);
      Assertions.assertEquals(OptionalInt.empty(), previousTrack);
    }

    @Test
    @DisplayName("if the index argument is higher than the size of the list then the empty optional should be returned")
    void ifTheIndexArgumentIsHigherThanTheSizeOfTheListThenTheEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of(zeroTrack,
          firstTrack));
      var previousTrack = queueManager.getPreviousTrack(2);
      Assertions.assertEquals(OptionalInt.empty(), previousTrack);
    }

  }

  @Nested
  class GetNextTrack{
    @Test
    @DisplayName("it should select the next tracks on the playlist")
    void itShouldSelectNextTrackOnThePlaylist() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack,
          fourthTrack, fifthTrack);
      final QueueManager queueManager = new OrderedPlaylistQueueManager(playlist);
      var nextTrack = queueManager.getNextTrack(2);
      Assertions.assertEquals(OptionalInt.of(3), nextTrack);
    }
    @Test
    @DisplayName("after the last tracks the empty optional should be returned")
    void afterLastTrackEmptyOptionalShouldBeReturned() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack,
          fourthTrack, fifthTrack);
      final QueueManager nextTrackSelector = new OrderedPlaylistQueueManager(playlist);
      var nextTrack = nextTrackSelector.getNextTrack(5);
      Assertions.assertEquals(OptionalInt.empty(), nextTrack);
    }

    @Test
    @DisplayName("if the playlist is empty then the empty optional should be returned")
    void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of());
      var nextTrack = queueManager.getNextTrack(1);
      Assertions.assertEquals(OptionalInt.empty(), nextTrack);
    }

    @Test
    @DisplayName("if the index argument is higher or equal the size of the list minus 1 then the empty optional should be returned")
    void ifTheIndexArgumentIsHigherThanTheSizeOfTheListThenTheEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of(zeroTrack,
          firstTrack));
      var nextTrack = queueManager.getNextTrack(1);
      Assertions.assertEquals(OptionalInt.empty(), nextTrack);
    }
  }

}
