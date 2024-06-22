package com.github.asciborek.last_fm;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.function.Supplier;

public class GetTokenTask implements Supplier<String> {

  private static final int HTTP_OK = 200;
  private static final int HTTP_FORBIDDEN = 403;

  private static final String REQUEST_TEMPLATE =
      "https://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=%s&format=json";
  private final HttpClient httpClient;
  private final String apiKey;
  private final ObjectMapper objectMapper;

  public GetTokenTask(HttpClient httpClient, String apiKey, ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.apiKey = apiKey;
    this.objectMapper = objectMapper;
  }

  @Override
  public String get() {
    var request = HttpRequest.newBuilder()
        .uri(URI.create(String.format(REQUEST_TEMPLATE, apiKey)))
        .GET()
        .build();
    try {
      var httpResponse = httpClient.send(request, BodyHandlers.ofString());
      return switch (httpResponse.statusCode()) {
        case HTTP_OK -> processResponseBody(httpResponse.body());
        case HTTP_FORBIDDEN -> throw new LastFmAuthenticationException("Invalid api key");
        default -> throw new LastFmAuthenticationException("Unknown error");
      };
    } catch (IOException | InterruptedException e) {
      throw new LastFmAuthenticationException("getToken failed", e);
    }
  }

  private String processResponseBody(String responseBody) {
    try {
      return objectMapper.readValue(responseBody, TokenResponse.class).token();
    } catch (IOException e) {
      throw new LastFmAuthenticationException("Error during parsing getToken response", e);
    }
  }

  private record TokenResponse(String token) {}
}
