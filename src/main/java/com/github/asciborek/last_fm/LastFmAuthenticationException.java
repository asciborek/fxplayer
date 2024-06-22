package com.github.asciborek.last_fm;

public class LastFmAuthenticationException extends RuntimeException {

  public LastFmAuthenticationException(String message) {
    super(message);
  }

  public LastFmAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
