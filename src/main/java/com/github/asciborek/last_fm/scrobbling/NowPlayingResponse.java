package com.github.asciborek.last_fm.scrobbling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public sealed interface NowPlayingResponse {

  record SuccessResponse() implements NowPlayingResponse {}

  record ErrorResponse(
      TrackApiErrorCode error,
      String errorMessage
  ) implements NowPlayingResponse {
    @JsonCreator
    public static ErrorResponse create(@JsonProperty("error") int errorCode, @JsonProperty("message") String errorMessage) {
      return new ErrorResponse(TrackApiErrorCode.fromCode(errorCode), errorMessage);
    }
  }
}
