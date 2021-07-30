package com.github.asciborek.metadata;

import com.google.common.io.Resources;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
  @DisplayName("should read the mp3 file meta data")
  void shouldReadMp3MetaData() throws Exception {
    File file = new File(Resources.getResource(TEST_MP3_FILEPATH).toURI());
    Track track = trackMetadataProvider.getMetadata(file).orElseThrow();
    Assertions.assertEquals(EXPECTED_TITLE, track.title());
    Assertions.assertEquals(EXPECTED_ALBUM, track.album());
    Assertions.assertEquals(EXPECTED_ARTIST, track.artist());
    Assertions.assertEquals(EXPECTED_LENGTH, track.length());
    Assertions.assertEquals("1_test_audio.mp3", track.fileName());
  }

  @Test
  @DisplayName("should read the mp4 file meta data")
  void shouldReadMp4MetaData() throws Exception {
    File file = new File(Resources.getResource(TEST_MP4_FILEPATH).toURI());
    Track track = trackMetadataProvider.getMetadata(file).orElseThrow();
    Assertions.assertEquals(EXPECTED_TITLE, track.title());
    Assertions.assertEquals(EXPECTED_ALBUM, track.album());
    Assertions.assertEquals(EXPECTED_ARTIST, track.artist());
    Assertions.assertEquals(EXPECTED_LENGTH, track.length());
    Assertions.assertEquals("2_test_audio.mp4", track.fileName());
  }


  @Test
  @DisplayName("should read the wav file meta data")
  void shouldReadWavMetaData() throws Exception {
    File file = new File(Resources.getResource(TEST_WAV_FILEPATH).toURI());
    Track track = trackMetadataProvider.getMetadata(file).orElseThrow();
    Assertions.assertEquals(EXPECTED_TITLE, track.title());
    Assertions.assertEquals(EXPECTED_ALBUM, track.album());
    Assertions.assertEquals(EXPECTED_ARTIST, track.artist());
    Assertions.assertEquals(EXPECTED_LENGTH, track.length());
    Assertions.assertEquals("3_test_audio.wav", track.fileName());
  }

}
