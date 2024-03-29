package com.github.asciborek.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DurationUtilsTest {

  @ParameterizedTest
  @MethodSource("formatTestArguments")
  @DisplayName("format duration to standard format")
  void formatDurationToStandardFormat(Duration duration, String formattedDuration) {
    assertThat(DurationUtils.format(duration)).isEqualTo(formattedDuration);
  }

  @ParameterizedTest
  @MethodSource("formatSecondsTestArguments")
  @DisplayName("format duration in seconds to standard format")
  void formatDurationInSecondsToStandardFormat(int seconds, String formattedDuration) {
    assertThat(DurationUtils.formatTimeInSeconds(seconds)).isEqualTo(formattedDuration);
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
