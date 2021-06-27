package com.github.asciborek.album_cover;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.CompletableFuture;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CachingAlbumCoverProvider implements AlbumCoverProvider {

  private static final Logger LOG = LoggerFactory.getLogger(CachingAlbumCoverProvider.class);
  private final AlbumCoverProvider delegate;
  private final AsyncCache<ArtistAlbum, Image> cache = Caffeine.newBuilder()
      .maximumSize(20)
      .buildAsync();

  CachingAlbumCoverProvider(AlbumCoverProvider delegate) {
    this.delegate = delegate;
  }

  @Override
  public CompletableFuture<Image> fetchAlbum(ArtistAlbum albumCoverRequest) {
    var image = cache.getIfPresent(albumCoverRequest);
    if (image == null) {
      LOG.info("the cover for {} was not found in cache", albumCoverRequest);
      image = delegate.fetchAlbum(albumCoverRequest);
      cache.put(albumCoverRequest, image);
    } else {
      LOG.info("the cover for {} loaded from cache", albumCoverRequest);
    }
    return image;
  }
}
