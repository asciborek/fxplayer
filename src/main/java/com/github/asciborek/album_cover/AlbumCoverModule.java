package com.github.asciborek.album_cover;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class AlbumCoverModule extends PrivateModule  {

  @Override
  protected void configure() {
    bind(AlbumCoverProvider.class).toProvider(AlbumCoverProviderFactory.class).in(Scopes.SINGLETON);
    bind(AlbumCoverController.class).toProvider(AlbumCoverControllerFactory.class).in(Scopes.SINGLETON);
    expose(AlbumCoverController.class);
  }
}
