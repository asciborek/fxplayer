package com.github.asciborek.artist_info;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LastFmArtistInfoProvider implements ArtistInfoProvider {

  private static final Logger LOG = LoggerFactory.getLogger(LastFmArtistInfoProvider.class);
  private static final String LAST_FM_REQUEST_TEMPLATE =
      "https://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=%s&api_key=%s&format=json";
  private static final int HTTP_OK = 200;

  private final HttpClient httpClient;
  private final String apiURI;
  private final String apiKey;

  LastFmArtistInfoProvider(HttpClient httpClient, String apiKey) {
    this(httpClient,  apiKey, LAST_FM_REQUEST_TEMPLATE);
  }

  LastFmArtistInfoProvider(HttpClient httpClient, String apiKey, String requestUriFormat) {
    this.httpClient = httpClient;
    this.apiURI = requestUriFormat;
    this.apiKey = apiKey;
  }

  @Override
  public CompletableFuture<ArtistInfo> getArtistInfo(String artistName) {
    LOG.info("send request for artist {} info", artistName);
    return httpClient.sendAsync(createRequest(artistName), BodyHandlers.ofString())
        .thenApply(this::parseResponse);
  }

  private HttpRequest createRequest(String artistName) {
    var requestUri = String.format(apiURI, artistName.replace(" ", "+"), apiKey);
    return HttpRequest.newBuilder()
        .uri(URI.create(requestUri))
        .GET()
        .build();
  }

  private ArtistInfo parseResponse(HttpResponse<String> response) {
    if (response.statusCode() != HTTP_OK) {
      throw new FetchLastFmArtistInfoException(response.statusCode());
    }
    return parseResponse(response.body());
  }

  private ArtistInfo parseResponse(String json) {
    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
    var errorElement = jsonObject.get("error");
    if (errorElement != null) {
      return ArtistInfo.NOT_FOUND;
    }
    return new ArtistInfo(parseDescription(jsonObject), parseSimilarArtists(jsonObject));
  }

  private String parseDescription(JsonObject jsonObject) {
    return jsonObject
        .get("artist").getAsJsonObject()
        .get("bio").getAsJsonObject()
        .get("content").getAsString();
  }

  private List<String> parseSimilarArtists(JsonObject jsonObject) {
    ImmutableList.Builder<String> similarArtistsBuilder = ImmutableList.builder();
    jsonObject.get("artist").getAsJsonObject()
        .get("similar").getAsJsonObject()
        .get("artist").getAsJsonArray()
        .forEach(element -> {
          var similarArtist = element.getAsJsonObject().get("name").getAsString();
          similarArtistsBuilder.add(similarArtist);
        });
    return similarArtistsBuilder.build();
  }


  static final class FetchLastFmArtistInfoException extends RuntimeException{
    FetchLastFmArtistInfoException(int statusCode) {
      super("Could not fetch last.fm artist info, the response status: " + statusCode);
    }
  }

}
