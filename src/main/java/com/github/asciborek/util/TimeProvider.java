package com.github.asciborek.util;

import java.time.Instant;
import java.time.ZoneId;

public interface TimeProvider {

  Instant currentTime();

  ZoneId applicationZoneId();

}
