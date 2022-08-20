package com.github.asciborek.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("UnstableApiUsage")
public class TrackMetadataProviderTest {

  private static final String EXPECTED_TITLE = "dummy";
  private static final String EXPECTED_ALBUM = "fxplayer";
  private static final String EXPECTED_ARTIST = "asciborek";
  private static final String EXPECTED_LENGTH = "00:30";
  private static final String TEST_MP3_FILEPATH = "data/audio/1_test_audio.mp3";
  private static final String TEST_MP4_FILEPATH = "data/audio/2_test_audio.mp4";
  private static final String TEST_WAV_FILEPATH = "data/audio/3_test_audio.wav";


  private final TrackMetadataProvider trackMetadataProvider = new TrackMetadataProvider();

  @Test
  @DisplayName("read the mp3 file meta data")
  void readMp3MetaData() throws Exception {
    File file = new File(Resources.getResource(TEST_MP3_FILEPATH).toURI());
    Track track = trackMetadataProvider.getMetadata(file).orElseThrow();
    assertThat(track.title()).isEqualTo(EXPECTED_TITLE);
    assertThat(track.album()).isEqualTo(EXPECTED_ALBUM);
    assertThat(track.artist()).isEqualTo(EXPECTED_ARTIST);
    assertThat(track.length()).isEqualTo(EXPECTED_LENGTH);
    assertThat(track.fileName()).isEqualTo("1_test_audio.mp3");
  }

  @Test
  @DisplayName("read the mp4 file meta data")
  void radMp4MetaData() throws Exception {
    File file = new File(Resources.getResource(TEST_MP4_FILEPATH).toURI());
    Track track = trackMetadataProvider.getMetadata(file).orElseThrow();
    assertThat(track.title()).isEqualTo(EXPECTED_TITLE);
    assertThat(track.album()).isEqualTo(EXPECTED_ALBUM);
    assertThat(track.artist()).isEqualTo(EXPECTED_ARTIST);
    assertThat(track.length()).isEqualTo(EXPECTED_LENGTH);
    assertThat(track.fileName()).isEqualTo("2_test_audio.mp4");
  }


  @Test
  @DisplayName("read the wav file meta data")
  void readWavMetaData() throws Exception {
    File file = new File(Resources.getResource(TEST_WAV_FILEPATH).toURI());
    Track track = trackMetadataProvider.getMetadata(file).orElseThrow();
    assertThat(track.title()).isEqualTo(EXPECTED_TITLE);
    assertThat(track.album()).isEqualTo(EXPECTED_ALBUM);
    assertThat(track.artist()).isEqualTo(EXPECTED_ARTIST);
    assertThat(track.length()).isEqualTo(EXPECTED_LENGTH);
    assertThat(track.fileName()).isEqualTo("3_test_audio.wav");
  }

}
