package com.github.asciborek.player;

import static com.google.common.io.Resources.getResource;

import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataProvider;
import com.github.asciborek.metadata.TrackMetadataUpdatedEvent;
import com.github.asciborek.player.PlayerCommand.AddDirectoryCommand;
import com.github.asciborek.player.PlayerCommand.AddTrackCommand;
import com.github.asciborek.player.PlayerCommand.ClearPlaylistCommand;
import com.github.asciborek.player.PlayerCommand.LoadPlaylistCommand;
import com.github.asciborek.player.PlayerCommand.OpenFileCommand;
import com.github.asciborek.player.PlayerCommand.OpenTrackFileCommand;
import com.github.asciborek.player.PlayerCommand.PlayOrPauseTrackCommand;
import com.github.asciborek.player.PlayerCommand.RemoveTrackCommand;
import com.github.asciborek.player.PlayerCommand.SavePlaylistCommand;
import com.github.asciborek.player.PlayerCommand.ShufflePlaylistCommand;
import com.github.asciborek.player.PlayerEvent.PlaylistOpenedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistShuffledEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.github.asciborek.playlist.PlaylistService;
import com.github.asciborek.util.FileUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlaylistController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(PlaylistController.class);

  private static final String PLAYLIST_AUTO_SAVE_FILENAME = "playlist_auto_save.plst";
  private static final String PLAYLIST_FILE_EXTENSION = ".plst";

  private final ObservableList<Track> tracksQueue;
  private final PlaylistService playlistService;
  private final EventBus eventBus;
  private final TrackMetadataProvider trackMetadataProvider;
  private final EditTrackPopupFactory editTrackPopUpFactory;

  @FXML
  private TableView<Track> playlistView;
  @FXML
  private TableColumn<Track, String> titleColumn;
  @FXML
  private TableColumn<Track, String> albumColumn;
  @FXML
  private TableColumn<Track, String> artistColumn;
  @FXML
  private TableColumn<Track, String> lengthColumn;
  @FXML
  private TableColumn<Track, String> filenameColumn;

  public PlaylistController(ObservableList<Track> tracksQueue, PlaylistService playlistService,
      EventBus eventBus, TrackMetadataProvider trackMetadataProvider,
      EditTrackPopupFactory editTrackPopUpFactory) {
    this.tracksQueue = tracksQueue;
    this.playlistService = playlistService;
    this.eventBus = eventBus;
    this.trackMetadataProvider = trackMetadataProvider;
    this.editTrackPopUpFactory = editTrackPopUpFactory;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    playlistService.loadPlaylistWithExistingFiles(playlistAutoSaveFile()).
        thenAccept(this::addTracksToPlaylist);
    playlistView.setItems(tracksQueue);
    setCellValueFactories();
  }

  public void onPlaylistMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getClickCount() == 2) {
      var selectedTrackIndex = getSelectedTrackIndex();
      var selectedTrack = getSelectedTrack();
      LOG.info("onPlaylistMouseClicked selectedTrack: {}", selectedTrack);
      eventBus.post(new PlayOrPauseTrackCommand(selectedTrack, selectedTrackIndex));
    }
  }

  public void onPlaylistKeyClicked(KeyEvent keyEvent) {
    switch (keyEvent.getCode()) {
      case SPACE -> playOrPauseSelectedTrack();
      case DELETE -> removeSelectedTrack();
    }
  }

  public void onEditTrackInformationMenuItem() {
    var selectedTrack = getSelectedTrack();
    if (selectedTrack != null) {
      LOG.info("onEditTrackInformation, the selected track: {}", selectedTrack);
      try {
        Stage popup = editTrackPopUpFactory.createPopupForm(selectedTrack);
        popup.show();
      } catch (Exception e) {
        LOG.error("couldn't load the edit tags form ", e);
      }
    }
  }

  public void onRemoveTrackMenuItem () {
    LOG.info("onRemoveTrackMenuItemClicked");
    removeSelectedTrack();
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onOpenFileCommand(OpenFileCommand openFileCommand) {
    final File file = openFileCommand.file();
    if (file.getPath().endsWith(PLAYLIST_FILE_EXTENSION)) {
      playlistService.loadPlaylistWithExistingFiles(file)
          .thenAccept(this::onOpenPlaylist);
    } else if (FileUtils.isSupportedAudioFile(file.getPath())) {
      trackMetadataProvider.getMetadata(file)
          .ifPresent(this::onOpenAudioFile);
    } else {
      LOG.info("a not supported file extensions for the file: {}", file.getPath());
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onAddTrackCommand(AddTrackCommand addTrackCommand) {
    playlistService.getTrack(addTrackCommand.trackFile())
        .ifPresent(tracksQueue::add);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onAddDirectoryCommand(AddDirectoryCommand addDirectoryCommand) {
    playlistService.getDirectoryTracks(addDirectoryCommand.directory())
        .thenAccept(this::addTracksToPlaylist);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onSavePlaylistCommand(SavePlaylistCommand savePlaylistCommand) {
    playlistService.savePlaylist(savePlaylistCommand.playlistFile(), tracksQueue);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onLoadPlaylistCommand(LoadPlaylistCommand loadPlaylistCommand) {
    playlistService.loadPlaylistWithExistingFiles(loadPlaylistCommand.playlistFile())
        .thenAccept(this::addTracksToPlaylist);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onClearPlaylistCommand(ClearPlaylistCommand clearPlaylistCommand) {
    LOG.info("Clear playlist. Removed items size: {}", tracksQueue.size());
    tracksQueue.clear();
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onShufflePlaylistCommand(ShufflePlaylistCommand shufflePlaylistCommand) {
    if (!tracksQueue.isEmpty()) {
      Collections.shuffle(tracksQueue);
      eventBus.post(new PlaylistShuffledEvent());
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onTrackMetadataUpdatedEvent(TrackMetadataUpdatedEvent event) {
    LOG.info("Track {} was updated, new data: {}", event.oldTrack(), event.newTrack());
    tracksQueue.replaceAll(track -> {
      if (track.equals(event.oldTrack())) {
        LOG.info("reloading the {} track data", track.filePath());
        return event.newTrack();
      }
      return track;
    });
    playlistView.refresh();
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onStartPlayingTrackEvent(StartPlayingTrackEvent event) {
    playlistView.getSelectionModel().select(event.track());
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onCloseExit(CloseApplicationEvent closeApplicationEvent) {
    LOG.info("save {} track(s) to the auto-save playlist", tracksQueue.size());
    playlistService.savePlaylist(playlistAutoSaveFile(), ImmutableList.copyOf(tracksQueue));
  }

  private void setCellValueFactories() {
    titleColumn.setCellValueFactory(this::getTitleProperty);
    albumColumn.setCellValueFactory(this::getAlbumProperty);
    artistColumn.setCellValueFactory(this::getArtistProperty);
    lengthColumn.setCellValueFactory(this::getLengthProperty);
    filenameColumn.setCellValueFactory(this::getFileNameProperty);
  }

  private void addTracksToPlaylist(Collection<Track> tracks) {
    Platform.runLater(() -> {
      tracksQueue.addAll(tracks);
      playlistView.refresh();
    });
  }

  private Track getSelectedTrack() {
    return playlistView.getSelectionModel().getSelectedItem();
  }

  private StringProperty getTitleProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().title());
  }

  private StringProperty getAlbumProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().album());
  }

  private StringProperty getArtistProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().artist());
  }

  private StringProperty getLengthProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().length());
  }

  private StringProperty getFileNameProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().fileName());
  }

  private File playlistAutoSaveFile() {
    return FileUtils.getApplicationDataDirectory().resolve(PLAYLIST_AUTO_SAVE_FILENAME).toFile();
  }

  private void playOrPauseSelectedTrack() {
    eventBus.post(new PlayOrPauseTrackCommand(getSelectedTrack(), getSelectedTrackIndex()));
  }

  private int getSelectedTrackIndex() {
    return playlistView.getSelectionModel().getFocusedIndex();
  }

  private void removeSelectedTrack() {
    var trackIndex = getSelectedTrackIndex();
    if (trackIndex >= 0) {
      eventBus.post(new RemoveTrackCommand(trackIndex));
    }
  }

  private void onOpenPlaylist(List<Track> loadedPlaylist) {
    Platform.runLater(() -> {
      tracksQueue.clear();
      if (!loadedPlaylist.isEmpty()) {
        tracksQueue.addAll(loadedPlaylist);
        playlistView.refresh();
        eventBus.post(new PlaylistOpenedEvent());
      }
    });
  }

  private void onOpenAudioFile(Track track) {
    tracksQueue.clear();
    tracksQueue.add(track);
    eventBus.post(new OpenTrackFileCommand(track));
  }

}
