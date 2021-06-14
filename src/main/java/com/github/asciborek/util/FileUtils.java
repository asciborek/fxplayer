package com.github.asciborek.util;

import static com.google.common.io.Files.getFileExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class FileUtils {

  private static final String USER_HOME = "user.home";
  private static final String TEMP_DIRECTORY = "java.io.tmpdir";
  private static final List<String> SUPPORTED_AUDIO_FILES_EXTENSIONS = List.of("mp3", "mp4", "wav");

  private FileUtils() {
  }

  public static String getUserHome() {
    return System.getProperty(USER_HOME);
  }

  public static String getTempDirectory() {
    return System.getProperty(TEMP_DIRECTORY);
  }

  public static Path getApplicationDataDirectory() {
    return Path.of(getUserHome(), ".fxplayer");
  }

  public static List<String> getSupportedAudioFilesExtensions() {
    return SUPPORTED_AUDIO_FILES_EXTENSIONS;
  }

  public static boolean isSupportedAudioFile(String path) {
    return hasSupportedExtension(path, SUPPORTED_AUDIO_FILES_EXTENSIONS);
  }

  public static Stream<Path> getDirectoryFilesWithSupportedExtensions(Path directoryPath,
      Iterable<String> extensions) throws IOException {
    if (!Files.isDirectory(directoryPath)) {
      return Stream.empty();
    }
    return Files.walk(directoryPath)
        .filter(Files::isRegularFile)
        .filter(file -> hasSupportedExtension(file.toString(), extensions));
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

  private static boolean hasSupportedExtension(String file, Iterable<String> extensions) {
    var fileExtension = getFileExtension(file);
    for (String extension : extensions) {
      if (extension.equals(fileExtension)) {
        return true;
      }
    }
    return false;
  }

}
