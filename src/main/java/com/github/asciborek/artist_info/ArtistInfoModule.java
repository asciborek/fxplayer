package com.github.asciborek.artist_info;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public final class ArtistInfoModule extends PrivateModule {

  @Override
  protected void configure() {
    bind(ArtistInfoProvider.class).toProvider(ArtistInfoProviderFactory.class).in(Scopes.SINGLETON);
    bind(ArtistInfoController.class).toProvider(ArtistInfoControllerFactory.class).in(Scopes.SINGLETON);
    expose(ArtistInfoController.class);
  }
}
