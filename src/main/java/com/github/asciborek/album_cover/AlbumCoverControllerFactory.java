package com.github.asciborek.album_cover;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("UnstableApiUsage")//Guava
final class AlbumCoverControllerFactory implements Provider<AlbumCoverController> {

  private final AlbumCoverProvider albumCoverProvider;
  private final EventBus eventBus;

  @Inject
  public AlbumCoverControllerFactory(AlbumCoverProvider albumCoverProvider, EventBus eventBus) {
    this.albumCoverProvider = albumCoverProvider;
    this.eventBus = eventBus;
  }

  @Override
  public AlbumCoverController get() {
    var controller = new AlbumCoverController(albumCoverProvider);
    eventBus.register(controller);
    return controller;
  }
}
