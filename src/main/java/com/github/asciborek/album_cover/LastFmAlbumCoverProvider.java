package com.github.asciborek.album_cover;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.Stopwatch;
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
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LastFmAlbumCoverProvider implements AlbumCoverProvider{

  private static final Logger LOG = LoggerFactory.getLogger(LastFmAlbumCoverProvider.class);
  private static final String LAST_FM_REQUEST_TEMPLATE =
      "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=%s&artist=%s&album=%s&format=json";
  private static final String IMAGE_SIZE = "mega";

  private final HttpClient lastFmHttpClient;
  private final ExecutorService executorService;
  private final ObjectReader objectReader;
  private final String apiUriTemplate;
  private final String apiKey;

  LastFmAlbumCoverProvider(HttpClient lastFmHttpClient, ExecutorService executorService,
      ObjectMapper objectMapper, String apiKey) {
    this(lastFmHttpClient, executorService, objectMapper, LAST_FM_REQUEST_TEMPLATE, apiKey);
  }

  LastFmAlbumCoverProvider(HttpClient lastFmHttpClient, ExecutorService executorService,
      ObjectMapper objectMapper, String apiUriTemplate, String apiKey) {
    this.lastFmHttpClient = lastFmHttpClient;
    this.executorService = executorService;
    this.objectReader = objectMapper.reader();
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
    try {
      var response = objectReader.readValue(json, AlbumResponse.class);
      if (response.error() != null){
        throw new FetchAlbumException();
      }
      return response.album().image()
          .stream()
          .filter(this::matchesSize)
          .findFirst()
          .map(ImageItem::text)
          .orElseThrow(FetchAlbumException::new);
    } catch (IOException e) {
      throw new FetchAlbumException();
    }
  }

  private boolean matchesSize(ImageItem imageItem) {
    return imageItem.size.equals(IMAGE_SIZE);
  }

  private record AlbumResponse(Integer error, Album album){}

  private record Album(List<ImageItem> image) {}

  private record ImageItem(String size, @JsonProperty("#text") String text){ }

}
