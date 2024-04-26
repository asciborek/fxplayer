package com.github.asciborek.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ALL")
public class FileUtilsTest {

  private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("mp3", "mp4");

  @Test
  @DisplayName("get recursively all files from the directory and filter by extension")
  void getDirectoryFilesWithSupportedExtensions() {
    var extensions = SUPPORTED_EXTENSIONS;
    var path = getPathFromResource();
    try (var fileStream = FileUtils.getDirectoryFilesWithSupportedExtensions(path, extensions)) {
      var fileList = fileStream.collect(Collectors.toList());
      assertThat(fileList).allMatch(this::matchExpression);
      assertThat(fileList.isEmpty()).isFalse();
    } catch (IOException e) {
      Assertions.fail("getDirectoryFilesWithSupportedExtensions thrown IO exception", e);
    }
  }


  @Test
  void pathsAreEqual() {
    var originalFilePath = Paths.get("/home/aleksander/analiza.txt");
    var directoryPath = Paths.get("/home/aleksander/");
    var fileName = "analiza.txt";
    var resolvedPath = directoryPath.resolve(fileName);
    assertThat(resolvedPath).isEqualTo(originalFilePath);
  }

  private boolean matchExpression(Path file) {
    return SUPPORTED_EXTENSIONS.contains(Files.getFileExtension(file.toString()));
  }

  private Path getPathFromResource() {
    try {
      return Path.of(Resources.getResource("data").toURI());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
