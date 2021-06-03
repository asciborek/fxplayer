package com.github.asciborek.artist_info;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class InMemoryArtistInfoProvider implements ArtistInfoProvider {
  
  private final Map<String, ArtistInfo> artistsData = Map.of(
      "Haken", new ArtistInfo("Haken are English progressive metal band",
          List.of("Dream Theater", "Leprous", "Richard Henshall", "Circus Maximus")),
      "Yes", new ArtistInfo("Yes are an English progressive rock band formed in London in 1968",
          List.of("Emerson, Lake & Palmer", "Genesis", "King Crimson", "Gentle Giant", "Camel", "Pink Floyd"))
  );

  @Override
  public CompletableFuture<ArtistInfo> getArtistInfo(String artistName) {
    return Optional
        .ofNullable(artistsData.get(artistName))
        .map(CompletableFuture::completedFuture)
        .orElse(CompletableFuture.completedFuture(ArtistInfo.NOT_FOUND));
  }
}
