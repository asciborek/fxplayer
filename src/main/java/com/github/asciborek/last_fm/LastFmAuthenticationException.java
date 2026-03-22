package com.github.asciborek.last_fm;

final class LastFmAuthenticationException extends RuntimeException {

  LastFmAuthenticationException(String message) {
    super(message);
  }

  LastFmAuthenticationException(int statusCode) {
    super("Could not fetch last.fm request token, response status: " + statusCode);
  }

  LastFmAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
