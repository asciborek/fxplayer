package com.github.asciborek.last_fm.authentication;

public sealed interface LastFmSessionResponse {

  record LastFmSessionSuccessResponse(Session session) implements LastFmSessionResponse {}

  record Session(String name, String key, String subscriber) {}

  record LastFmErrorResponse(
      String message,
      int error
  ) implements LastFmSessionResponse {}

}
