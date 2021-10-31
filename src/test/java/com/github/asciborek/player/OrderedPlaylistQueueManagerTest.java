package com.github.asciborek.player;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.asciborek.metadata.Track;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
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
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack, fourthTrack, fifthTrack);
      final QueueManager queueManager = new OrderedPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(2);
      assertThat(previousTrack).isEqualTo(OptionalInt.of(1));
    }

    @Test
    @DisplayName("it should return the empty optional for the first tracks")
    void itShouldReturnTheEmptyOptionalForTheFirstTrack() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack, fourthTrack, fifthTrack);
      final QueueManager queueManager = new OrderedPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(0);
      assertThat(previousTrack).isEqualTo(OptionalInt.empty());
    }

    @Test
    @DisplayName("if the playlist is empty then the empty optional should be returned")
    void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of());
      var previousTrack = queueManager.getPreviousTrack(0);
      assertThat(previousTrack).isEqualTo(OptionalInt.empty());
    }

    @Test
    @DisplayName("if the index argument is higher than the size of the list then the empty optional should be returned")
    void ifTheIndexArgumentIsHigherThanTheSizeOfTheListThenTheEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of(zeroTrack, firstTrack));
      var previousTrack = queueManager.getPreviousTrack(2);
      assertThat(previousTrack).isEqualTo(OptionalInt.empty());
    }

  }

  @Nested
  class GetNextTrack{
    @Test
    @DisplayName("it should select the next tracks on the playlist")
    void itShouldSelectNextTrackOnThePlaylist() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack, fourthTrack, fifthTrack);
      final QueueManager queueManager = new OrderedPlaylistQueueManager(playlist);
      var nextTrack = queueManager.getNextTrack(2);
      assertThat(nextTrack).isEqualTo(OptionalInt.of(3));
    }
    @Test
    @DisplayName("after the last tracks the empty optional should be returned")
    void afterLastTrackEmptyOptionalShouldBeReturned() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack, fourthTrack, fifthTrack);
      final QueueManager nextTrackSelector = new OrderedPlaylistQueueManager(playlist);
      var nextTrack = nextTrackSelector.getNextTrack(5);
      assertThat(nextTrack).isEqualTo(OptionalInt.empty());
    }

    @Test
    @DisplayName("if the playlist is empty then the empty optional should be returned")
    void ifPlaylistIsEmptyThenEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of());
      var nextTrack = queueManager.getNextTrack(1);
      assertThat(nextTrack).isEqualTo(OptionalInt.empty());
    }

    @Test
    @DisplayName("if the index argument is higher or equal the size of the list minus 1 then the empty optional should be returned")
    void ifTheIndexArgumentIsHigherThanTheSizeOfTheListThenTheEmptyOptionalShouldBeReturned() {
      final QueueManager queueManager = new OrderedPlaylistQueueManager(List.of(zeroTrack, firstTrack));
      var nextTrack = queueManager.getNextTrack(1);
      assertThat(nextTrack).isEqualTo(OptionalInt.empty());
    }
  }

}
