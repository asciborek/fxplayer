package com.github.asciborek.util;

import java.time.Duration;

public final class DurationUtils {

  private DurationUtils() {}

  public static String format(Duration duration) {
    if (duration.toHoursPart() > 0) {
      return String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
    }
    return String.format("%02d:%02d", duration.toMinutesPart(), duration.toSecondsPart());
  }

}
