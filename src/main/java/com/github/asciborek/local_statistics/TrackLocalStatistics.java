package com.github.asciborek.local_statistics;

import java.time.Instant;
import java.util.Optional;

public record TrackLocalStatistics(long totalCount, Long firstPlayedTimestamp, Long lastPlayedTimestamp) {

  public Optional<Instant> firstPlayed() {
    if (firstPlayedTimestamp != null) {
      return Optional.of(Instant.ofEpochSecond(firstPlayedTimestamp));
    }
    return Optional.empty();
  }

  public Optional<Instant> lastPlayed() {
    if (lastPlayedTimestamp != null) {
      return Optional.of(Instant.ofEpochSecond(lastPlayedTimestamp));
    }
    return Optional.empty();
  }

}
