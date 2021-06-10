package com.github.asciborek.player;

import com.github.asciborek.player.OrderedPlaylistQueueManager;
import com.github.asciborek.player.QueueManager;
import com.github.asciborek.playlist.Track;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderedPlaylistQueueManagerTest {

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
  
  @Nested
  class GetPreviousTrack {
    @Test
    @DisplayName("it should select the previous tracks from the list")
    void itShouldSelectPreviousTrackFromTheList() {
      final List<Track> playlist = List.of(firstTrack, secondTrack, thirdTrack, fourthTrack,
          fifthTrack, sixthTrack);
      final QueueManager queueManager = new OrderedPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(thirdTrack);
      Assertions.assertEquals(Optional.of(secondTrack), previousTrack);
    }

    @Test
    @DisplayName("it should return the empty optional for the first tracks")
    void itShouldReturnTheEmptyOptionalForTheFirstTrack() {
      final List<Track> playlist = List.of(firstTrack, secondTrack, thirdTrack, fourthTrack,
          fifthTrack, sixthTrack);
      final QueueManager queueManager = new OrderedPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(firstTrack);
      Assertions.assertEquals(Optional.empty(), previousTrack);
    }

    @Test
    @DisplayName("if the playlist is empty then the empty optional should be returned")
    void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of());
      var previousTrack = queueManager.getPreviousTrack(firstTrack);
      Assertions.assertEquals(Optional.empty(), previousTrack);
    }

    @Test
    @DisplayName("if the playlist does not contain the tracks then the empty optional should be returned")
    void ifPlaylistDoesNotContainTrackThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of(secondTrack, thirdTrack));
      var previousTrack = queueManager.getPreviousTrack(fourthTrack);
      Assertions.assertEquals(Optional.empty(), previousTrack);
    }
  }

  @Nested
  class GetNextTrack {
    @Test
    @DisplayName("it should select the next tracks on the playlist")
    void itShouldSelectNextTrackOnThePlaylist() {
      final List<Track> playlist = List.of(firstTrack, secondTrack, thirdTrack, fourthTrack,
          fifthTrack, sixthTrack);
      final QueueManager queueManager = new OrderedPlaylistQueueManager(playlist);
      var nextTrack = queueManager.getNextTrack(secondTrack);
      Assertions.assertEquals(Optional.of(thirdTrack), nextTrack);
    }
    @Test
    @DisplayName("after the last tracks the empty optional should be returned")
    void afterLastTrackEmptyOptionalShouldBeReturned() {
      final List<Track> playlist = List.of(firstTrack, secondTrack, thirdTrack, fourthTrack,
          fifthTrack, sixthTrack);
      final QueueManager nextTrackSelector = new OrderedPlaylistQueueManager(playlist);
      var nextTrack = nextTrackSelector.getNextTrack(sixthTrack);
      Assertions.assertEquals(Optional.empty(), nextTrack);
    }

    @Test
    @DisplayName("if the playlist is empty then the empty optional should be returned")
    void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of());
      var nextTrack = queueManager.getNextTrack(firstTrack);
      Assertions.assertEquals(Optional.empty(), nextTrack);
    }

    @Test
    @DisplayName("if the playlist does not contain the tracks then the empty optional should be returned")
    void ifPlaylistDoesNotContainTrackThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of(thirdTrack, fourthTrack));
      var nextTrack = queueManager.getNextTrack(firstTrack);
      Assertions.assertEquals(Optional.empty(), nextTrack);
    }
  }

}
