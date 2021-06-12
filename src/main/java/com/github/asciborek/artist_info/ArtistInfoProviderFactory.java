package com.github.asciborek.artist_info;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.net.http.HttpClient;

public final class ArtistInfoProviderFactory implements Provider<ArtistInfoProvider> {

  private final HttpClient httpClient;
  private final String lastFmApiKey;

  @Inject
  public ArtistInfoProviderFactory(HttpClient httpClient, String lastFmApiKey) {
    this.httpClient = httpClient;
    this.lastFmApiKey = lastFmApiKey;
  }

  @Override
  public ArtistInfoProvider get() {
    var lastFmArtistInfoProvider = new LastFmArtistInfoProvider(httpClient, lastFmApiKey);
    return new CachingArtistInfoProvider(lastFmArtistInfoProvider);
  }

}
