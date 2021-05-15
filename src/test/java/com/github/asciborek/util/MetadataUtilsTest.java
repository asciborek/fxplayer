package com.github.asciborek.util;

import com.github.asciborek.playlist.Track;
import com.google.common.io.Resources;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetadataUtilsTest {

  private static final String TEST_AUDIO_FILENAME = "data/audio/1_test_audio.mp3";

  @Test
  void shouldReadMetaData() throws Exception {
    File file =  new File(Resources.getResource(TEST_AUDIO_FILENAME).toURI());
    Track track = MetadataUtils.getTrackMetaData(file).orElseThrow();
    Assertions.assertEquals("dummy", track.title());
    Assertions.assertEquals("fxplayer", track.album());
    Assertions.assertEquals("asciborek", track.artist());
    Assertions.assertEquals("00:30", track.length());
    Assertions.assertEquals("1_test_audio.mp3", track.fileName());
  }

}
