package com.github.asciborek.player;

import com.github.asciborek.playlist.PlaylistService;
import com.github.asciborek.playlist.PlaylistServiceFactory;
import com.github.asciborek.playlist.Track;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class PlayerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PlaylistService.class).toProvider(PlaylistServiceFactory.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public ObservableList<Track> playlist() {
    return FXCollections.observableArrayList();
  }

}
