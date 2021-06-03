package com.github.asciborek.artist_info;

import java.util.concurrent.CompletableFuture;

public interface ArtistInfoProvider {

  CompletableFuture<ArtistInfo> getArtistInfo(String artistName);
}
