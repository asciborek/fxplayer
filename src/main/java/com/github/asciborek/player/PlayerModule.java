package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataProvider;
import com.github.asciborek.metadata.TrackMetadataUpdater;
import com.github.asciborek.playlist.PlaylistService;
import com.github.asciborek.playlist.PlaylistServiceFactory;
import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.util.TimeProvider;
import com.google.common.eventbus.EventBus;
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
  public ObservableList<Track> tracksQueue() {
    return FXCollections.observableArrayList();
  }

  @Provides
  @Singleton
  public MainWindowController mainWindowController(EventBus eventBus, SettingsService settingsService) {
    var controller = new MainWindowController(eventBus, settingsService);
    eventBus.register(controller);
    return controller;
  }

  @Provides
  @Singleton
  public EditTrackPopupFactory editTrackPopupFactory (TrackMetadataUpdater trackMetadataUpdater,
      EventBus eventBus) {
    return new EditTrackPopupFactory(trackMetadataUpdater, eventBus);
  }

  @Provides
  @Singleton
  public PlaylistController playlistController(ObservableList<Track> tracksQueue,
      PlaylistService playlistService, EventBus eventBus, TrackMetadataProvider trackMetadataProvider,
      EditTrackPopupFactory editTrackPopUpFactory) {
    var controller = new PlaylistController(tracksQueue, playlistService, eventBus,
        trackMetadataProvider, editTrackPopUpFactory);
    eventBus.register(controller);
    return controller;
  }

  @Provides
  @Singleton
  public AudioPlayerController audioPlayerController(EventBus eventBus,
      SettingsService settingsService, ObservableList<Track> tracksQueue,
      TimeProvider timeProvider) {
    var controller = new AudioPlayerController(eventBus, settingsService, tracksQueue, timeProvider);
    eventBus.register(controller);
    return controller;
  }

}
