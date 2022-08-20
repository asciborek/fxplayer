package com.github.asciborek.player;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.asciborek.metadata.Track;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
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
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack, fourthTrack, fifthTrack);
      final QueueManager queueManager = new RepeatPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(3);
      assertThat(previousTrack).isEqualTo(OptionalInt.of(2));
    }

    @Test
    @DisplayName("return the last element on the list when the previous track was the first track")
    void returnTheLastElementOnListWhenThePreviousTrackWasTheFirstTrack() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack, fourthTrack, fifthTrack);
      final QueueManager queueManager = new RepeatPlaylistQueueManager(playlist);
      var previousTrack = queueManager.getPreviousTrack(0);
      assertThat(previousTrack).isEqualTo(OptionalInt.of(5));
    }

    @Test
    @DisplayName("return the empty optional for the empty playlist")
    void returnEmptyOptionalForTheEmptyPlaylist() {
      final QueueManager queueManager = new RepeatPlaylistQueueManager(List.of());
      var previousTrack = queueManager.getPreviousTrack(1);
      assertThat(previousTrack).isEqualTo(OptionalInt.empty());
    }

    @Test
    @DisplayName("return empty optional when index is higher than the size of the list")
    void returnEmptyOptionalWhenIndexIsHigherThanTheSizeOfTheList()  {
      final QueueManager queueManager = new RepeatPlaylistQueueManager(List.of(zeroTrack, firstTrack));
      var previousTrack = queueManager.getPreviousTrack(2);
      assertThat(previousTrack).isEqualTo(OptionalInt.empty());
    }
  }

  @Nested
  class GetNextTrack {

    @Test
    @DisplayName("select the next trac on the playlist")
    void selectTheNextTrackOnThePlaylist() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack, fourthTrack, fifthTrack);
      final QueueManager queueManager = new RepeatPlaylistQueueManager(playlist);
      var nextTrack = queueManager.getNextTrack(2);
      assertThat(nextTrack).isEqualTo(OptionalInt.of(3));
    }

    @Test
    @DisplayName("after the last track on the list return the first track")
    void afterTheLastTrackOnTheListReturnTheFirstTrack() {
      final List<Track> playlist = List.of(zeroTrack, firstTrack, secondTrack, thirdTrack, fourthTrack, fifthTrack);
      final QueueManager nextTrackSelector = new RepeatPlaylistQueueManager(playlist);
      var nextTrack = nextTrackSelector.getNextTrack(5);
      assertThat(nextTrack).isEqualTo(OptionalInt.of(0));
    }

    @Test
    @DisplayName("return the empty optional for the empty playlist")
    void returnEmptyOptionalForTheEmptyPlaylist() {
      final QueueManager queueManager = new RepeatPlaylistQueueManager(List.of());
      var previousTrack = queueManager.getNextTrack(1);
      assertThat(previousTrack).isEqualTo(OptionalInt.empty());
    }

    @Test
    @DisplayName("return empty optional when index is higher than the size of the list")
    void returnEmptyOptionalWhenIndexIsHigherThanTheSizeOfTheList() {
      final QueueManager queueManager = new RepeatPlaylistQueueManager(List.of(zeroTrack, firstTrack));
      var previousTrack = queueManager.getNextTrack(2);
      assertThat(previousTrack).isEqualTo(OptionalInt.empty());
    }
  }
}
