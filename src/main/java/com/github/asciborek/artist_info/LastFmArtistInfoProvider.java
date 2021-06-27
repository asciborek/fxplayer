package com.github.asciborek.artist_info;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LastFmArtistInfoProvider implements ArtistInfoProvider {

  private static final Logger LOG = LoggerFactory.getLogger(LastFmArtistInfoProvider.class);
  private static final String LAST_FM_REQUEST_TEMPLATE =
      "https://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=%s&api_key=%s&format=json";
  private static final int HTTP_OK = 200;

  private final HttpClient httpClient;
  private final ExecutorService executorService;
  private final String apiURI;
  private final String apiKey;

  LastFmArtistInfoProvider(HttpClient httpClient, ExecutorService executorService, String apiKey) {
    this(httpClient, executorService,  apiKey, LAST_FM_REQUEST_TEMPLATE);
  }

  LastFmArtistInfoProvider(HttpClient httpClient, ExecutorService executorService, String apiKey, String requestUriFormat) {
    this.httpClient = httpClient;
    this.executorService = executorService;
    this.apiURI = requestUriFormat;
    this.apiKey = apiKey;
  }

  @Override
  public CompletableFuture<ArtistInfo> getArtistInfo(String artistName) {
    LOG.info("send request for artist {} info", artistName);
    return CompletableFuture.supplyAsync(() -> sendRequest(artistName), executorService)
        .thenApply(this::parseResponse);
  }

  private HttpResponse<String> sendRequest(String artistName) {
    var stopwatch = Stopwatch.createStarted();
    var request = createRequest(artistName);
    try {
      return httpClient.send(request, BodyHandlers.ofString());
    } catch (InterruptedException e) {
      throw new FetchLastFmArtistInfoException(e.getMessage());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      var elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
      LOG.info("get artist ({}) info, elapsed time: {} (ms)", artistName, elapsed);
    }
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
    var stopWatch = Stopwatch.createStarted();
    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
    var errorElement = jsonObject.get("error");
    if (errorElement != null) {
      return ArtistInfo.NOT_FOUND;
    }
    var result = new ArtistInfo(parseDescription(jsonObject), parseSimilarArtists(jsonObject));
    var elapsed = stopWatch.stop().elapsed(TimeUnit.MILLISECONDS);
    LOG.info("after parse artist info, elapsed time: {} (ms)", elapsed);
    return result;
  }

  private String parseDescription(JsonObject jsonObject) {
    return jsonObject
        .get("artist").getAsJsonObject()
        .get("bio").getAsJsonObject()
        .get("content").getAsString();
  }

  private List<String> parseSimilarArtists(JsonObject jsonObject) {
    ImmutableList.Builder<String> similarArtistsBuilder = ImmutableList.builder();
    var jsonArray = jsonObject.get("artist").getAsJsonObject()
        .get("similar").getAsJsonObject()
        .get("artist").getAsJsonArray();
    for (JsonElement element: jsonArray) {
      var similarArtist = element.getAsJsonObject().get("name").getAsString();
      similarArtistsBuilder.add(similarArtist);
    }
    return similarArtistsBuilder.build();
  }


  static final class FetchLastFmArtistInfoException extends RuntimeException{
    FetchLastFmArtistInfoException(String message) {
      super(message);
    }
    FetchLastFmArtistInfoException(int statusCode) {
      super("Could not fetch last.fm artist info, the response status: " + statusCode);
    }
  }

}
