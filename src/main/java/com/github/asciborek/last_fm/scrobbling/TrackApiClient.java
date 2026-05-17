package com.github.asciborek.last_fm.scrobbling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.asciborek.metadata.Track;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.TreeMap;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(TrackApiClient.class);

  private static final String LAST_FM_API_URL = "https://ws.audioscrobbler.com/2.0/";
  private static final int HTTP_OK = 200;

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String sharedSecret;

  public TrackApiClient(HttpClient httpClient, ObjectMapper objectMapper, String apiKey, String sharedSecret) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
    this.sharedSecret = sharedSecret;
  }

  public NowPlayingResponse sendUpdateNowPlayingRequest(Track track, String sessionToken) {

    var signatureParams = new TreeMap<String, String>();
    signatureParams.put("method", "track.updateNowPlaying");
    signatureParams.put("artist", track.artist());
    signatureParams.put("track", track.title());
    signatureParams.put("album", track.album());
    signatureParams.put("api_key", apiKey);
    signatureParams.put("sk", sessionToken);

    String body = buildFormEncodedBody(signatureParams);
    HttpResponse<String> response = sendRequest(body);
    LOG.info("track.updateNowPlaying response body: {}", response.body());
    if (response.statusCode() == HTTP_OK) {
      return new NowPlayingResponse.SuccessResponse();
    }
    return mapToObject(response.body(), NowPlayingResponse.ErrorResponse.class);

  }

  public ScrobbleResponse sendScrobbleTracksRequest(List<Scrobble> scrobbles, String sessionToken) {
    TreeMap<String, String>  signatureParams = createSignatureParamsForTrackScrobbleMethod(scrobbles, sessionToken);

    String body = buildFormEncodedBody(signatureParams);
    HttpResponse<String> response = sendRequest(body);
    LOG.info("track.scrobble response body: {}", response.body());
    if (response.statusCode() == HTTP_OK) {
      return mapToObject(response.body(), ScrobbleResponse.SuccessResponse.class);
    }

    return mapToObject(response.body(), ScrobbleResponse.ErrorResponse.class);
  }

  private @NonNull TreeMap<String, String> createSignatureParamsForTrackScrobbleMethod(List<Scrobble> scrobbles,
      String sessionToken) {
    var signatureParams = new TreeMap<String, String>();
    signatureParams.put("method", "track.scrobble");
    signatureParams.put("api_key", apiKey);
    signatureParams.put("sk", sessionToken);
    // Add each track with indexed parameters: artist[0], track[0], timestamp[0], etc.

    int i = 0;

    for (Scrobble scrobble: scrobbles) {
      signatureParams.put("artist[" + i + "]", scrobble.artist());
      signatureParams.put("track[" + i + "]", scrobble.track());
      signatureParams.put("timestamp[" + i + "]", String.valueOf(scrobble.timestamp()));
      if (scrobble.album() != null && !scrobble.album().isEmpty()) {
        signatureParams.put("album[" + i + "]", scrobble.album());
      }
      i++;
    }

    return signatureParams;
  }

  private <T> T mapToObject(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      LOG.debug("Failed to parse JSON response", e);
      return null;
    }
  }

  private HttpResponse<String> sendRequest(String body)  {
    var request = HttpRequest.newBuilder()
        .uri(URI.create(LAST_FM_API_URL))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    try {
      return httpClient.send(request, BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


  private String generateSignature(TreeMap<String, String> params) {
    var signatureBuilder = new StringBuilder();

    // TreeMap keeps keys in sorted order
    for (var entry : params.entrySet()) {
      signatureBuilder.append(entry.getKey()).append(entry.getValue());
    }
    signatureBuilder.append(sharedSecret);

    try {
      var signatureDigest = MessageDigest.getInstance("MD5")
          .digest(signatureBuilder.toString().getBytes(StandardCharsets.UTF_8));
      var hex = new StringBuilder(signatureDigest.length * 2);
      for (var signatureByte : signatureDigest) {
        hex.append(String.format("%02x", signatureByte & 0xff));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm unavailable for Last.fm API signature", e);
    }
  }


  /**
   * Build form-encoded request body.
   */
  private String buildFormEncodedBody(TreeMap<String, String> signatureParams) {
    String signature = generateSignature(signatureParams);
    var allParams = new TreeMap<>(signatureParams);
    allParams.put("api_sig", signature);
    allParams.put("format", "json");
    var bodyBuilder = new StringBuilder();
    boolean first = true;
    for (var entry : allParams.entrySet()) {
      if (!first) {
        bodyBuilder.append("&");
      }
      bodyBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
          .append("=")
          .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
      first = false;
    }
    return bodyBuilder.toString();
  }
}
