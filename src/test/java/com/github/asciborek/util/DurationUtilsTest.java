package com.github.asciborek.util;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DurationUtilsTest {

  @ParameterizedTest
  @MethodSource("formatTestArguments")
  @DisplayName("duration should be formatted to standard format")
  void shouldFormatDurationToStandardFormat(Duration duration, String formattedDuration) {
    Assertions.assertEquals(formattedDuration, DurationUtils.format(duration));
  }

  @ParameterizedTest
  @MethodSource("formatSecondsTestArguments")
  @DisplayName("duration should be formatted to standard format")
  void shouldFormatDurationInSecondsToStandardFormat(int seconds, String formattedDuration) {
    Assertions.assertEquals(formattedDuration, DurationUtils.formatTimeInSeconds(seconds));
  }

  private static Stream<Arguments> formatSecondsTestArguments() {
    return Stream.of(
        Arguments.of(820, "13:40"),
        Arguments.of(451, "07:31"),
        Arguments.of(940, "15:40"),
        Arguments.of(3662, "01:01:02")
    );
  }

  private static Stream<Arguments> formatTestArguments() {
    return Stream.of(
      Arguments.of(Duration.ofSeconds(820), "13:40"),
      Arguments.of(Duration.ofSeconds(451), "07:31"),
      Arguments.of(Duration.ofSeconds(940), "15:40"),
      Arguments.of(Duration.ofSeconds(3662), "01:01:02")
    );
  }

}
