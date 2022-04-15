package com.github.asciborek.util;

import java.time.Instant;
import java.time.ZoneId;

public final class ConstantTimeProvider implements TimeProvider {

  private final ZoneId warsawZoneId = ZoneId.of("Europe/Warsaw");
  private final Instant time;

  public ConstantTimeProvider(Instant time) {
    this.time = time;
  }

  @Override
  public ZoneId applicationZoneId() {
    return warsawZoneId;
  }

  @Override
  public Instant currentTime() {
    return time;
  }
}
