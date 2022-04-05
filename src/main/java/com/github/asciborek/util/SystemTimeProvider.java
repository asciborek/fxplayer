package com.github.asciborek.util;

import java.time.Instant;

public final class SystemTimeProvider implements TimeProvider {

  @Override
  public Instant currentTime() {
    return Instant.now();
  }

  @Override
  public String toString() {
    return "SystemTimeProvider";
  }
}
