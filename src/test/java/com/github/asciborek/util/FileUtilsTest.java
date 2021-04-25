package com.github.asciborek.util;

import com.google.common.io.Resources;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FileUtilsTest {

  private static final String MP3_EXTENSION = ".mp3";

  @Test
  @DisplayName("it should get recursively all files from the directory and filter by extension")
  void shouldGetDirectoryFilesWithSupportedExtensions() {
    var extensions = Set.of(MP3_EXTENSION);
    var path = getPathFromResource();
    try (var fileStream = FileUtils.getDirectoryFilesWithSupportedExtensions(path, extensions)) {
      var fileList = fileStream.collect(Collectors.toList());
      Assertions.assertTrue(allMatchesMp3Extension(fileList));
      Assertions.assertFalse(fileList.isEmpty());
    } catch (Exception e) {
      Assertions.fail(e);
    }
  }

  private boolean allMatchesMp3Extension(List<Path> files) {
    return files.stream()
        .allMatch(file -> file.toString().endsWith(MP3_EXTENSION));
  }

  private Path getPathFromResource() {
    try {
      return Path.of(Resources.getResource("data").toURI());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
