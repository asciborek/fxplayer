package com.github.asciborek.album_cover;

import com.google.common.base.Stopwatch;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LastFmAlbumCoverProvider implements AlbumCoverProvider{

  private static final Logger LOG = LoggerFactory.getLogger(LastFmAlbumCoverProvider.class);
  private static final String LAST_FM_REQUEST_TEMPLATE =
      "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=%s&artist=%s&album=%s&format=json";
  private static final String IMAGE_SIZE = "mega";
  private static final String JSON_SIZE_ELEMENT = "size";
  private static final String JSON_IMAGE_LINK_ELEMENT = "#text";
  private static final String JSON_ALBUM_ELEMENT = "album";
  private static final String JSON_IMAGE_ARRAY = "image";

  private final HttpClient lastFmHttpClient;
  private final ExecutorService executorService;
  private final String apiUriTemplate;
  private final String apiKey;

  LastFmAlbumCoverProvider(HttpClient lastFmHttpClient, ExecutorService executorService, String apiKey) {
    this(lastFmHttpClient, executorService, LAST_FM_REQUEST_TEMPLATE, apiKey);
  }

  LastFmAlbumCoverProvider(HttpClient lastFmHttpClient, ExecutorService executorService, String apiUriTemplate, String apiKey) {
    this.lastFmHttpClient = lastFmHttpClient;
    this.executorService = executorService;
    this.apiUriTemplate = apiUriTemplate;
    this.apiKey = apiKey;
  }

  @Override
  public CompletableFuture<Image> fetchAlbum(ArtistAlbum albumCoverRequest) {
    return CompletableFuture.supplyAsync(() -> sendRequest(albumCoverRequest), executorService)
        .thenApply(this::getAlbumCover);
  }

  private HttpResponse<String> sendRequest(ArtistAlbum albumCoverRequest) {
    var httpRequest = createRequest(albumCoverRequest);
    var stopWatch = Stopwatch.createStarted();
    try {
      return lastFmHttpClient.send(httpRequest, BodyHandlers.ofString());
    } catch (InterruptedException e) {
      throw new FetchAlbumException();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      var elapsedTime = stopWatch.stop().elapsed(TimeUnit.MILLISECONDS);
      LOG.info("get album info {}, elapsed time: {} (ms)", albumCoverRequest, elapsedTime);
    }
  }

  private HttpRequest createRequest(ArtistAlbum albumCoverRequest) {
    var artistName = albumCoverRequest.artist().replace(" ", "+");
    var albumName = albumCoverRequest.album().replace(" ", "+");
    var requestUri = String.format(apiUriTemplate, apiKey, artistName, albumName);
    return HttpRequest.newBuilder()
        .uri(URI.create(requestUri))
        .GET()
        .build();
  }

  private Image getAlbumCover(HttpResponse<String> response) {
    var stopWatch = Stopwatch.createStarted();
    var url = extractLink(response.body());
    var image = new Image(url);
    var elapsed = stopWatch.stop().elapsed(TimeUnit.MILLISECONDS);
    LOG.info("loaded {} image, elapsed time: {} (ms)", url, elapsed);
    return image;
  }

  private String extractLink(String json) {
    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
    var errorElement = jsonObject.get("error");
    if (errorElement != null) {
      throw new FetchAlbumException();
    }
    var array = jsonObject
        .get(JSON_ALBUM_ELEMENT).getAsJsonObject()
        .get(JSON_IMAGE_ARRAY).getAsJsonArray();
    for (JsonElement element: array) {
      var size = element.getAsJsonObject().get(JSON_SIZE_ELEMENT).getAsString();
      if (IMAGE_SIZE.equals(size)) {
        return (element.getAsJsonObject().get(JSON_IMAGE_LINK_ELEMENT).getAsString());
      }
    }
    throw new FetchAlbumException();
  }

}
