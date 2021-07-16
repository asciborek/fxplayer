package com.github.asciborek.player;

import com.github.asciborek.playlist.Track;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class RepeatPlaylistQueueManagerTest {

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
      final QueueManager queueManager = new RepeatPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(3);
      Assertions.assertEquals(OptionalInt.of(2), previousTrack);
    }

    @Test
    @DisplayName("the previous track for the first track should be the last track")
    void itShouldReturnTheLastTrackForTheFirstTrack() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack,
          fourthTrack, fifthTrack);
      final QueueManager queueManager = new RepeatPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(0);
      Assertions.assertEquals(OptionalInt.of(5), previousTrack);
    }

    @Test
    @DisplayName("if the playlist is empty then the empty optional should be returned")
    void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new RepeatPlaylistQueueManager(List.of());
      var previousTrack = queueManager.getPreviousTrack(1);
      Assertions.assertEquals(OptionalInt.empty(), previousTrack);
    }

    @Test
    @DisplayName("if the index argument is higher than the size of the list then the empty optional should be returned")
    void ifTheIndexArgumentIsHigherThanTheSizeOfTheListThenTheEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new RepeatPlaylistQueueManager(List.of(zeroTrack, firstTrack));
      var previousTrack = queueManager.getPreviousTrack(2);
      Assertions.assertEquals(OptionalInt.empty(), previousTrack);
    }
  }

  @Nested
  class GetNextTrack {

    @Test
    @DisplayName("it should select the next tracks on the playlist")
    void itShouldSelectNextTrackOnThePlaylist() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack,
          fourthTrack, fifthTrack);
      final QueueManager queueManager = new RepeatPlaylistQueueManager(playlist);
      var nextTrack = queueManager.getNextTrack(2);
      Assertions.assertEquals(OptionalInt.of(3), nextTrack);
    }

    @Test
    @DisplayName("after the last track the first track should be returned")
    void afterTheLastTrackTheFirstOptionalShouldBeReturned() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack,
          fourthTrack, fifthTrack);
      final QueueManager nextTrackSelector = new RepeatPlaylistQueueManager(playlist);
      var nextTrack = nextTrackSelector.getNextTrack(5);
      Assertions.assertEquals(OptionalInt.of(0), nextTrack);
    }

    @Test
    @DisplayName("if the playlist is empty then the empty optional should be returned")
    void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new RepeatPlaylistQueueManager(List.of());
      var previousTrack = queueManager.getNextTrack(1);
      Assertions.assertEquals(OptionalInt.empty(), previousTrack);
    }

    @Test
    @DisplayName("if the index argument is higher than the size of the list then the empty optional should be returned")
    void ifTheIndexArgumentIsHigherThanTheSizeOfTheListThenTheEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new RepeatPlaylistQueueManager(List.of(zeroTrack, firstTrack));
      var previousTrack = queueManager.getNextTrack(2);
      Assertions.assertEquals(OptionalInt.empty(), previousTrack);
    }
  }
}
