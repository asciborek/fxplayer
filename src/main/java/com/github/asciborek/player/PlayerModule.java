package com.github.asciborek.player;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataProvider;
import com.github.asciborek.metadata.TrackMetadataUpdater;
import com.github.asciborek.playlist.PlaylistService;
import com.github.asciborek.playlist.PlaylistServiceFactory;
import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.util.TimeProvider;
import com.google.common.eventbus.EventBus;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class PlayerModule extends PrivateModule {

  private final EventBus eventBus;
  private final ExecutorService executorService;

  public PlayerModule(EventBus eventBus, ExecutorService executorService) {
    this.eventBus = eventBus;
    this.executorService = executorService;
  }

  @Override
  protected void configure() {
    bind(PlaylistService.class).toProvider(PlaylistServiceFactory.class).in(Scopes.SINGLETON);
    bind(TracksFilesWatcher.class).toProvider(this::createTracksFilesWatcher).asEagerSingleton();
  }

  private TracksFilesWatcher createTracksFilesWatcher() {
    var tracksFilesWatcher = new TracksFilesWatcher(eventBus, executorService);
    eventBus.register(tracksFilesWatcher);
    return tracksFilesWatcher;
  }

  @Provides
  @Singleton
  public ObservableList<Track> tracksQueue() {
    return FXCollections.observableArrayList();
  }

  @Provides
  @Singleton
  @Exposed
  public MainWindowController mainWindowController(EventBus eventBus, SettingsService settingsService) {
    var controller = new MainWindowController(eventBus, settingsService);
    eventBus.register(controller);
    return controller;
  }

  @Provides
  @Singleton
  EditTrackPopupFactory editTrackPopupFactory (TrackMetadataUpdater trackMetadataUpdater) {
    return new EditTrackPopupFactory(trackMetadataUpdater, eventBus);
  }

  @Provides
  @Singleton
  @Exposed
  PlaylistController playlistController(ObservableList<Track> tracksQueue,
      PlaylistService playlistService, TrackMetadataProvider trackMetadataProvider,
      EditTrackPopupFactory editTrackPopUpFactory) {
    var controller = new PlaylistController(tracksQueue, playlistService, eventBus,
        trackMetadataProvider, editTrackPopUpFactory);
    eventBus.register(controller);
    return controller;
  }

  @Provides
  @Singleton
  @Exposed
  public AudioPlayerController audioPlayerController(SettingsService settingsService,
      ObservableList<Track> tracksQueue,
      TimeProvider timeProvider) {
    var controller = new AudioPlayerController(eventBus, settingsService, tracksQueue, timeProvider);
    eventBus.register(controller);
    return controller;
  }

}
