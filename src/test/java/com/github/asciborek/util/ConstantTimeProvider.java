package com.github.asciborek.util;

import java.time.Instant;

public final class ConstantTimeProvider implements TimeProvider {

  private final Instant time;

  public ConstantTimeProvider(Instant time) {
    this.time = time;
  }

  @Override
  public Instant currentTime() {
    return time;
  }
}
