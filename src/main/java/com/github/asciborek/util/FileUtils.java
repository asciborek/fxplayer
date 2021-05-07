package com.github.asciborek.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtils {

  private static final String USER_HOME = "user.home";

  private FileUtils() {
  }

  public static String getUserHome() {
    return System.getProperty(USER_HOME);
  }

  public static Path getApplicationDataDirectory() {
    return Path.of(getUserHome(), ".fxplayer");
  }

  public static Stream<Path> getDirectoryFilesWithSupportedExtensions(Path directoryPath,
      Iterable<String> extensions) throws IOException {
    if (!Files.isDirectory(directoryPath)) {
      return Stream.empty();
    }
    return Files.walk(directoryPath)
        .filter(Files::isRegularFile)
        .filter(file -> hasSupportedExtension(file, extensions));
  }

  public static void createDirectory(Path path) {
    try {
      Files.createDirectory(path);
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  public static void createFile(Path path) {
    try {
      Files.createFile(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }



  private static boolean hasSupportedExtension(Path file, Iterable<String> extensions) {
    for (String extension : extensions) {
      if (file.toString().endsWith(extension)) {
        return true;
      }
    }
    return false;
  }

}
