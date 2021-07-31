package com.github.asciborek.album_cover;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;

final class AlbumCoverProviderFactory implements Provider<AlbumCoverProvider> {

  private final HttpClient httpClient;
  private final ExecutorService executorService;
  private final String lastFmApiKey;

  @Inject
  public AlbumCoverProviderFactory(HttpClient httpClient, ExecutorService executorService, String lastFmApiKey) {
    this.httpClient = httpClient;
    this.lastFmApiKey = lastFmApiKey;
    this.executorService = executorService;
  }

  @Override
  public AlbumCoverProvider get() {
    var delegate = new LastFmAlbumCoverProvider(httpClient, executorService, lastFmApiKey);
    return new CachingAlbumCoverProvider(delegate);
  }
}
