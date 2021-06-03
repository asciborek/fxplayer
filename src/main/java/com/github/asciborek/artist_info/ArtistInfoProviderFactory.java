package com.github.asciborek.artist_info;

import com.google.inject.Provider;

public class ArtistInfoProviderFactory implements Provider<ArtistInfoProvider> {

  @Override
  public ArtistInfoProvider get() {
    return new InMemoryArtistInfoProvider();
  }
}
