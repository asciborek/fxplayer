package com.github.asciborek.player;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class PlayerModule extends AbstractModule {

  @Provides
  @Singleton
  public ObservableList<Track> playlist() {
    return FXCollections.observableArrayList();
  }

  @Provides
  @Singleton
  public PlaylistService playlistService(ExecutorService executorService) {
    return new PlaylistService(executorService, FileExtension.getSupportedExtensions());
  }

}
