package com.github.asciborek.util;

import java.time.Instant;

public interface TimeProvider {

  Instant currentTime();

}
