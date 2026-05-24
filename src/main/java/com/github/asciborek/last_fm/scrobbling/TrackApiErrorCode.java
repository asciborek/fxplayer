package com.github.asciborek.last_fm.scrobbling;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public enum TrackApiErrorCode {
  INVALID_SERVICE(2, "Invalid service"),
  INVALID_METHOD(3, "Invalid method"),
  AUTHENTICATION_FAILED(4, "Authentication failed"),
  INVALID_FORMAT(5, "Invalid format"),
  INVALID_PARAMETERS(6, "Invalid parameters"),
  INVALID_RESOURCE_SPECIFIED(7, "Invalid resource specified"),
  OPERATION_FAILED(8, "Operation failed"),
  INVALID_SESSION_KEY(9, "Invalid session key"),
  INVALID_API_KEY(10, "Invalid API key"),
  SERVICE_OFFLINE(11, "Service offline"),
  INVALID_METHOD_SIGNATURE(13, "Invalid method signature"),
  TEMPORARY_ERROR(16, "Temporary error"),
  RATE_LIMIT_EXCEEDED(15, "Rate limit exceeded"),
  API_KEY_SUSPENDED(26, "API key suspended"),
  UNKNOWN(99, "Unknown error");

  private static final Map<Integer, TrackApiErrorCode> CODE_ERROR_MAP;

  static {
    var mapBuilder = ImmutableMap.<Integer, TrackApiErrorCode> builder();
    for (TrackApiErrorCode error : values()) {
      mapBuilder.put(error.code, error);
    }
    CODE_ERROR_MAP = mapBuilder.build();
  }

  private final int code;
  private final String message;

  TrackApiErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }


  public String getMessage() {
    return message;
  }

  public static TrackApiErrorCode fromCode(int code) {
    return CODE_ERROR_MAP.getOrDefault(code, UNKNOWN);
  }
}

