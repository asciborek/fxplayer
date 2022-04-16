package com.github.asciborek.local_statistics;

import java.time.Instant;
import java.util.Optional;

public record TrackLocalStatistics(long totalCount, Long firstPlayedEpochMilli, Long lastPlayedEpochMilli) {

  public Optional<Instant> firstPlayed() {
    if (firstPlayedEpochMilli != null) {
      return Optional.of(Instant.ofEpochMilli(firstPlayedEpochMilli));
    }
    return Optional.empty();
  }

  public Optional<Instant> lastPlayed() {
    if (firstPlayedEpochMilli != null) {
      return Optional.of(Instant.ofEpochMilli(lastPlayedEpochMilli));
    }
    return Optional.empty();
  }

}
