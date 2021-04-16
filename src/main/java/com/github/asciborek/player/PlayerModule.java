package com.github.asciborek.player;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PlayerModule extends AbstractModule {

  @Provides
  @Singleton
  public ObservableList<Track> playlist() {
    return FXCollections.observableArrayList();
  }

}
