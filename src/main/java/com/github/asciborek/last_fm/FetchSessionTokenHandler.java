package com.github.asciborek.last_fm;

import static java.net.HttpURLConnection.HTTP_OK;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class FetchSessionTokenHandler implements Consumer<LastFmToken> {

  private static final Logger LOG = LoggerFactory.getLogger(FetchSessionTokenHandler.class);

  private static final String AUTH_SESSION_URI_TEMPLATE =
      "https://ws.audioscrobbler.com/2.0/?method=auth.getSession&api_key=%s&token=%s&api_sig=%s&format=json";
  private static final int TOKEN_NOT_AUTHORIZED = 14;


  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String sharedSecret;

  public FetchSessionTokenHandler(HttpClient httpClient, ObjectMapper objectMapper, String apiKey,
      String sharedSecret) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
    this.sharedSecret = sharedSecret;
  }


  @Override
  public void accept(LastFmToken lastFmToken) {
    RetryPolicy<LastFmSessionResponse> retryPolicy = RetryPolicy.<LastFmSessionResponse>builder()
        .handleResultIf(this::shouldRetry)
        .withDelay(Duration.of(3, ChronoUnit.SECONDS))
        .withMaxAttempts(10)
        .build();
    var tokenResponse = Failsafe.with(retryPolicy).get(() -> fetchLastFmSessionResponse(lastFmToken.token()));
    LOG.info("get session token response {}", tokenResponse);
  }

  private boolean shouldRetry(LastFmSessionResponse response) {
    if (response instanceof LastFmSessionResponse.LastFmErrorResponse errorResponse) {
      return errorResponse.error() == TOKEN_NOT_AUTHORIZED;
    }
    return false;
  }

  private LastFmSessionResponse fetchLastFmSessionResponse(String token) {
    var requestUri = String.format(
        AUTH_SESSION_URI_TEMPLATE,
        URLEncoder.encode(apiKey, StandardCharsets.UTF_8),
        URLEncoder.encode(token, StandardCharsets.UTF_8),
        URLEncoder.encode(apiSignature(token), StandardCharsets.UTF_8));
    var request = HttpRequest.newBuilder()
        .uri(URI.create(requestUri))
        .GET()
        .build();

    try {
      var response = httpClient.send(request, BodyHandlers.ofString());
      String responseBody = response.body();
      if (response.statusCode() != HTTP_OK) {
        LOG.info("received session token error response: {}", responseBody);
        return objectMapper.readValue(responseBody, LastFmSessionResponse.LastFmErrorResponse.class);
      }
      LOG.info("Received session token response: {}", responseBody);
      return objectMapper.readValue(responseBody, LastFmSessionResponse.LastFmSessionSuccessResponse.class);
    } catch (IOException e) {
      throw new LastFmAuthenticationException("Error while fetching last.fm session token", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new LastFmAuthenticationException("Interrupted while fetching last.fm session token", e);
    }
  }

  private String apiSignature(String token) {
    var signatureSource = "api_key" + apiKey
        + "methodauth.getSession"
        + "token" + token
        + sharedSecret;

    try {
      var signatureDigest = MessageDigest.getInstance("MD5")
          .digest(signatureSource.getBytes(StandardCharsets.UTF_8));
      var hex = new StringBuilder(signatureDigest.length * 2);
      for (var signatureByte : signatureDigest) {
        hex.append(String.format("%02x", signatureByte & 0xff));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new LastFmAuthenticationException("MD5 algorithm unavailable for last.fm API signature", e);
    }
  }
}
