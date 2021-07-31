package com.github.asciborek.artist_info;

import java.util.concurrent.CompletableFuture;

interface ArtistInfoProvider {

  CompletableFuture<ArtistInfo> getArtistInfo(String artistName);
}
