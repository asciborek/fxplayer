package com.github.asciborek.playlist;

import com.google.common.io.Resources;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DirectoryTracksLoaderTest {

  @Test
  void itShouldLoadAllAudioFilesFromADirectory() {
    var directoryTracksLoader = new DirectoryTracksLoader(getPathFromResource(), List.of(".mp3"));
    var result = directoryTracksLoader.get();
    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals("1_test_audio.mp3", result.get(0).fileName());
    Assertions.assertEquals("2_test_audio.mp3", result.get(1).fileName());
  }

  private Path getPathFromResource() {
    try {
      return Path.of(Resources.getResource("data").toURI());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
