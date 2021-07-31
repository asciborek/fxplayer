package com.github.asciborek.artist_info;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("UnstableApiUsage")//Guava
final class ArtistInfoControllerFactory implements Provider<ArtistInfoController> {

  private final ArtistInfoProvider artistInfoProvider;
  private final EventBus eventBus;

  @Inject
  public ArtistInfoControllerFactory(ArtistInfoProvider artistInfoProvider, EventBus eventBus) {
    this.artistInfoProvider = artistInfoProvider;
    this.eventBus = eventBus;
  }

  @Override
  public ArtistInfoController get() {
    return new ArtistInfoController(artistInfoProvider, eventBus);
  }
}
