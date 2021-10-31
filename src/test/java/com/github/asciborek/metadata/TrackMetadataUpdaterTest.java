package com.github.asciborek.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TrackMetadataUpdaterTest {
  private static final String EXPECTED_TITLE = "dummy_updated";
  private static final String EXPECTED_ALBUM = "fxplayer_updated";
  private static final String EXPECTED_ARTIST = "asciborek_updated";
  private static final String EXPECTED_LENGTH = "00:30";
  private static final String TEST_MP3_FILEPATH = "data/audio/1_test_audio.mp3";
  private static final String TEST_MP4_FILEPATH = "data/audio/2_test_audio.mp4";
  private static final String TEST_WAV_FILEPATH = "data/audio/3_test_audio.wav";

  private final TrackMetadataProvider trackMetadataProvider = new TrackMetadataProvider(List.of(
      new Mp3AudioFileMetadataProvider(), new Mp4AudioFileMetadataProvider(),
      new WavAudioFileMetadataProvider()));

  private final TrackMetadataUpdater trackMetadataUpdater = new TrackMetadataUpdater();

  @DisplayName("should update mp3 tags")
  @Test
  void shouldUpdateMp3Tags() throws Exception {
    //Prepare a new file
    File file =  new File(Resources.getResource(TEST_MP3_FILEPATH).toURI());
    var copiedFilePath = Files.createTempFile("dummy_updated", ".mp3");
    Files.copy(file.toPath(), copiedFilePath, StandardCopyOption.REPLACE_EXISTING);
    Track trackWithOldMetadata = trackMetadataProvider.getMetadata(copiedFilePath)
        .orElseThrow();
    //Update metadata
    Track trackBeforeCommittedUpdate = trackWithOldMetadata.toBuilder()
        .withTitle(EXPECTED_TITLE)
        .withAlbum(EXPECTED_ALBUM)
        .withArtist(EXPECTED_ARTIST)
        .build();
    trackMetadataUpdater.updateTrackMedata(trackBeforeCommittedUpdate);
    Track trackAfterUpdate =  trackMetadataProvider.getMetadata(trackBeforeCommittedUpdate.filePath())
        .orElseThrow();
    //Assertions
    assertThat(trackAfterUpdate.title()).isEqualTo(EXPECTED_TITLE);
    assertThat(trackAfterUpdate.album()).isEqualTo(EXPECTED_ALBUM);
    assertThat(trackAfterUpdate.artist()).isEqualTo(EXPECTED_ARTIST);
    assertThat(trackAfterUpdate.length()).isEqualTo(EXPECTED_LENGTH);
  }

  @DisplayName("should update mp4 tags")
  @Test
  void shouldUpdateMp4Tags() throws Exception {
    //Prepare a new file
    File file = new File(Resources.getResource(TEST_MP4_FILEPATH).toURI());
    var copiedFilePath = Files.createTempFile("dummy_updated", ".mp4");
    Files.copy(file.toPath(), copiedFilePath, StandardCopyOption.REPLACE_EXISTING);
    Track trackWithOldMetadata = trackMetadataProvider.getMetadata(copiedFilePath)
        .orElseThrow();
    //Update metadata
    Track trackBeforeCommittedUpdate = trackWithOldMetadata.toBuilder()
        .withTitle(EXPECTED_TITLE)
        .withAlbum(EXPECTED_ALBUM)
        .withArtist(EXPECTED_ARTIST)
        .build();
    trackMetadataUpdater.updateTrackMedata(trackBeforeCommittedUpdate);
    Track trackAfterUpdate = trackMetadataProvider.getMetadata(trackBeforeCommittedUpdate.filePath())
        .orElseThrow();
    //Assertions
    assertThat(trackAfterUpdate.title()).isEqualTo(EXPECTED_TITLE);
    assertThat(trackAfterUpdate.album()).isEqualTo(EXPECTED_ALBUM);
    assertThat(trackAfterUpdate.artist()).isEqualTo(EXPECTED_ARTIST);
    assertThat(trackAfterUpdate.length()).isEqualTo(EXPECTED_LENGTH);
  }

  @DisplayName("should update wav tags")
  @Test
  void shouldUpdateWavTags() throws Exception {
    //Prepare a new file
    File file = new File(Resources.getResource(TEST_WAV_FILEPATH).toURI());
    var copiedFilePath = Files.createTempFile("dummy_updated", ".wav");
    Files.copy(file.toPath(), copiedFilePath, StandardCopyOption.REPLACE_EXISTING);
    Track trackWithOldMetadata = trackMetadataProvider.getMetadata(copiedFilePath)
        .orElseThrow();
    //Update metadata
    Track trackBeforeCommittedUpdate = trackWithOldMetadata.toBuilder()
        .withTitle(EXPECTED_TITLE)
        .withAlbum(EXPECTED_ALBUM)
        .withArtist(EXPECTED_ARTIST)
        .build();
    trackMetadataUpdater.updateTrackMedata(trackBeforeCommittedUpdate);
    Track trackAfterUpdate = trackMetadataProvider.getMetadata(trackBeforeCommittedUpdate.filePath())
        .orElseThrow();
    //Assertions
    assertThat(trackAfterUpdate.title()).isEqualTo(EXPECTED_TITLE);
    assertThat(trackAfterUpdate.album()).isEqualTo(EXPECTED_ALBUM);
    assertThat(trackAfterUpdate.artist()).isEqualTo(EXPECTED_ARTIST);
    assertThat(trackAfterUpdate.length()).isEqualTo(EXPECTED_LENGTH);
  }

}
