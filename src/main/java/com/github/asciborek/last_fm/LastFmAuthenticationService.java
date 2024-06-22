package com.github.asciborek.last_fm;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LastFmAuthenticationService {

  private static final Logger LOG = LoggerFactory.getLogger(LastFmAuthenticationService.class);

  private static final int HTTP_OK = 200;
  private static final int HTTP_FORBIDDEN = 403;
  private static final String GET_TOKEN_REQUEST_TEMPLATE = "https://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=%s&format=json";
  private static final String LAST_FM_AUTH_PAGE_URL_TEMPLATE = "http://www.last.fm/api/auth/?api_key=%s&token=%s";


  private final HttpClient httpClient;
  private final ExecutorService executorService;
  private final ObjectMapper objectMapper;
  private final String apiKey;

  public LastFmAuthenticationService(HttpClient httpClient,
      ExecutorService executorService,
      ObjectMapper objectMapper,
      String apiKey) {
    this.httpClient = httpClient;
    this.executorService = executorService;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
  }

  public void signInToLastFm() {
    CompletableFuture.supplyAsync(this::getToken, executorService)
        .whenComplete((token, throwable) -> {
          if (throwable != null) {
            LOG.error("getToken failed", throwable);
          } else {
            LOG.info("getToken token value: {}", token);
          }
        });
  }

  private String getToken() {
    var request = HttpRequest.newBuilder()
        .uri(URI.create(String.format(GET_TOKEN_REQUEST_TEMPLATE, apiKey)))
        .GET()
        .build();
    try {
      var httpResponse = httpClient.send(request, BodyHandlers.ofString());
      return switch (httpResponse.statusCode()) {
        case HTTP_OK -> processTokenResponseBody(httpResponse.body());
        case HTTP_FORBIDDEN -> throw new LastFmAuthenticationException("Invalid api key");
        default -> throw new LastFmAuthenticationException("Unknown error");
      };
    } catch (IOException | InterruptedException e) {
      throw new LastFmAuthenticationException("getToken failed", e);
    }
  }

  private String processTokenResponseBody(String responseBody) {
    try {
      return objectMapper.readValue(responseBody, TokenResponse.class).token();
    } catch (IOException e) {
      throw new LastFmAuthenticationException("Error during parsing getToken response", e);
    }
  }

  private record TokenResponse(String token) {}

}
