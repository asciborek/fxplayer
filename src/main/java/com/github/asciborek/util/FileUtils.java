package com.github.asciborek.util;

import java.nio.file.Path;

public final class FileUtils {

  private FileUtils() {}

  public static boolean hasSupportedExtension(Path file, Iterable<String> extensions) {
    for (String extension: extensions) {
      if (file.toString().endsWith(extension)) {
        return true;
      }
    }
    return false;
  }

}
