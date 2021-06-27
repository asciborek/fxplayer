package com.github.asciborek.artist_info;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;

public final class ArtistInfoProviderFactory implements Provider<ArtistInfoProvider> {

  private final HttpClient httpClient;
  private final ExecutorService executorService;
  private final String lastFmApiKey;

  @Inject
  public ArtistInfoProviderFactory(HttpClient httpClient, ExecutorService executorService, String lastFmApiKey) {
    this.httpClient = httpClient;
    this.executorService = executorService;
    this.lastFmApiKey = lastFmApiKey;
  }

  @Override
  public ArtistInfoProvider get() {
    var lastFmArtistInfoProvider = new LastFmArtistInfoProvider(httpClient, executorService, lastFmApiKey);
    return new CachingArtistInfoProvider(lastFmArtistInfoProvider);
  }

}
