package com.github.asciborek.artist_info;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CachingArtistInfoProvider implements ArtistInfoProvider {

  private static final Logger LOG = LoggerFactory.getLogger(CachingArtistInfoProvider.class);
  private final ArtistInfoProvider delegate;
  private final AsyncCache<String, ArtistInfo> cache = Caffeine.newBuilder()
      .maximumSize(20)
      .buildAsync();

  CachingArtistInfoProvider(ArtistInfoProvider delegate) {
    this.delegate = delegate;
  }

  @Override
  public CompletableFuture<ArtistInfo> getArtistInfo(String artistName) {
    var artistInfo = cache.getIfPresent(artistName);
    if (artistInfo == null) {
      LOG.info("artist {} not found in cache ", artistName);
      artistInfo = delegate.getArtistInfo(artistName);
      cache.put(artistName, artistInfo);
    } else {
      LOG.info("load artist {} info from cache", artistName);
    }
    return artistInfo;
  }

}
