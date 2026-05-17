package com.github.asciborek.last_fm.scrobbling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public sealed interface ScrobbleResponse {

  record SuccessResponse(Scrabbles scrobbles) implements ScrobbleResponse {}

  record Scrabbles(@JsonProperty("@attr") Attr attr) {}

  record Attr(int ignored, int accepted) {}

  record ErrorResponse(
      TrackApiErrorCode error,
      String errorMessage
  ) implements ScrobbleResponse {
    @JsonCreator
    public static ErrorResponse create(@JsonProperty("error") int errorCode, @JsonProperty("message") String errorMessage) {
      return new ErrorResponse(TrackApiErrorCode.fromCode(errorCode), errorMessage);
    }
  }
}

