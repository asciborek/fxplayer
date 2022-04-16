package com.github.asciborek.util;

import java.time.Instant;
import java.time.ZoneId;

public final class SystemTimeProvider implements TimeProvider {

  private final ZoneId zoneId = ZoneId.systemDefault();

  @Override
  public Instant currentTime() {
    return Instant.now();
  }

  @Override
  public ZoneId applicationZoneId() {
    return zoneId;
  }

  @Override
  public String toString() {
    return "SystemTimeProvider";
  }
}
