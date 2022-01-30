package com.github.asciborek.player;

import static com.google.common.io.Resources.getResource;
import static javafx.scene.input.KeyCombination.keyCombination;

import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.metadata.EditTagsController;
import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataProvider;
import com.github.asciborek.metadata.TrackMetadataUpdatedEvent;
import com.github.asciborek.metadata.TrackMetadataUpdater;
import com.github.asciborek.player.PlayerCommand.OpenTrackFileCommand;
import com.github.asciborek.player.PlayerCommand.PlayOrPauseTrackCommand;
import com.github.asciborek.player.PlayerCommand.RemoveTrackCommand;
import com.github.asciborek.player.PlayerEvent.PlaylistOpenedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistShuffledEvent;
import com.github.asciborek.player.PlayerEvent.ShowSidebarChangeEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.github.asciborek.playlist.PlaylistService;
import com.github.asciborek.settings.SettingsService;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")//Guava EventBus
public final class MainWindowController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(MainWindowController.class);

  private static final String PLAYLIST_AUTO_SAVE_FILENAME = "playlist_auto_save.plst";
  private static final String PLAYLIST_FILE_EXTENSION = ".plst";
  private static final String EDIT_TAGS_FXML = "fxml/edit_tags.fxml";

  private static final String OPEN_FILE_KEY_COMBINATION = "Ctrl + O";
  private static final String ADD_TRACK_KEY_COMBINATION = "Ctrl + Shift + A";
  private static final String ADD_DIRECTORY_KEY_COMBINATION = "Ctrl + Shift + D";
  private static final String CLEAR_PLAYLIST_COMBINATION = "Ctrl + Shift + Q";

  private static final ExtensionFilter AUDIO_FILES_FILTER = new ExtensionFilter(
      "audio files (mp3, mp4, wav", List.of("*.mp3", "*.wav", "*.mp4"));
  private static final ExtensionFilter MUSIC_FILTER = new ExtensionFilter(
      "Music", List.of("*.mp3", "*.wav", "*.mp4", "*.plst"));
  private static final ExtensionFilter PLAYLIST_EXTENSION_FILTER = new ExtensionFilter(
      "playlist files (*.plst)", "*.plst");

  private static final int SIDEBAR_WIDTH = 300;
  private static final int HIDDEN_SIDEBAR_WIDTH = 0;

  private final EventBus eventBus;
  private final TrackMetadataProvider trackMetadataProvider;
  private final TrackMetadataUpdater trackMetadataUpdater;
  private final PlaylistService playlistService;
  private final SettingsService settingsService;
  private final ObservableList<Track> tracksQueue;

  //Music Menu
  @FXML
  private MenuItem openFileMenuItem;

  // Playlist Menu
  @FXML
  private MenuItem addTrackMenuItem;
  @FXML
  private MenuItem addDirectoryMenuItem;
  @FXML
  private MenuItem clearPlaylistMenuItem;

  //Tools Menu
  @FXML
  private CheckMenuItem showSidebarMenuItem;
  //Embedded views
  @FXML
  private VBox sidebar;
  //Playlist UI
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

  @Inject
  public MainWindowController(EventBus eventBus, TrackMetadataProvider trackMetadataProvider,
      TrackMetadataUpdater trackMetadataUpdater, PlaylistService playlistService,
      SettingsService settingsService, ObservableList<Track> playlist) {
    this.trackMetadataProvider = trackMetadataProvider;
    this.trackMetadataUpdater = trackMetadataUpdater;
    this.playlistService = playlistService;
    this.eventBus = eventBus;
    this.settingsService = settingsService;
    this.tracksQueue = playlist;
    eventBus.register(this);
  }

  public void initialize(URL url, ResourceBundle resourceBundle) {
    playlistService.loadPlaylistWithExistingFiles(playlistAutoSaveFile()).
        thenAccept(this::addTracksToPlaylist);
    playlistView.setItems(tracksQueue);
    setCellValueFactories();
    registerKeyCombinations();
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onCloseExit(CloseApplicationEvent closeApplicationEvent) {
    LOG.info("save {} track(s) to the auto-save playlist", tracksQueue.size());
    playlistService.savePlaylist(playlistAutoSaveFile(), ImmutableList.copyOf(tracksQueue));
  }

  public void openFile() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(MUSIC_FILTER);
    fileChooser.setInitialDirectory(settingsService.getOpenFileFileChooserInitDirectory());
    var selectedFile = fileChooser.showOpenDialog(new Popup());
    if (selectedFile != null) {
      settingsService.setOpenFileFileChooserInitDirectory(selectedFile.getParentFile());
      onOpenFile(selectedFile);
    }
  }

  private void onOpenFile(File file) {
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

  public void addTrack() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(AUDIO_FILES_FILTER);
    fileChooser.setInitialDirectory(settingsService.getAddTrackFileChooserInitDirectory());
    var selectedFile = fileChooser.showOpenDialog(new Popup());
    if (selectedFile != null) {
      settingsService.setAddTrackFileChooserInitDirectory(selectedFile.getParentFile());
      playlistService.getTrack(selectedFile)
          .ifPresent(tracksQueue::add);
    }
  }

  public void addDirectory() {
    var directoryChooser = new DirectoryChooser();
    directoryChooser
        .setInitialDirectory(settingsService.getDirectoryDirectoryChooserInitDirectory());
    var selectedDirectory = directoryChooser.showDialog(new Popup());
    if (selectedDirectory != null) {
      settingsService
          .setAddDirectoryDirectoryChooserInitDirectory(selectedDirectory.getParentFile());
      playlistService.getDirectoryTracks(selectedDirectory)
          .thenAccept(this::addTracksToPlaylist);
    }
  }

  public void clearPlaylist() {
    LOG.info("Clear playlist. Removed items size: {}", tracksQueue.size());
    tracksQueue.clear();
  }

  public void shufflePlaylist() {
    if (!tracksQueue.isEmpty()) {
      Collections.shuffle(tracksQueue);
      eventBus.post(new PlaylistShuffledEvent());
    }
  }

  public void savePlaylist() {
    var playlistToSave = ImmutableList.copyOf(tracksQueue);
    var fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(new File(FileUtils.getUserHome()));
    fileChooser.getExtensionFilters().add(PLAYLIST_EXTENSION_FILTER);
    var playlistFile = fileChooser.showSaveDialog(new Popup());
    playlistService.savePlaylist(playlistFile, playlistToSave);
  }

  public void loadPlaylist() {
    var fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(new File(FileUtils.getUserHome()));
    fileChooser.getExtensionFilters().add(PLAYLIST_EXTENSION_FILTER);
    var playlistFile = fileChooser.showOpenDialog(new Popup());
    if (playlistFile.exists()) {
      playlistService.loadPlaylistWithExistingFiles(playlistFile)
          .thenAccept(this::addTracksToPlaylist);
    }
  }

  public void onShowSidebarChange() {
    boolean showSidebar = showSidebarMenuItem.isSelected();
    LOG.info("onShowSidebar, set visible to  {}", showSidebar);
    sidebar.setVisible(showSidebar);
    if (!showSidebar) {
      sidebar.setMinWidth(HIDDEN_SIDEBAR_WIDTH);
      sidebar.setMaxWidth(HIDDEN_SIDEBAR_WIDTH);
    } else {
      sidebar.setMinWidth(SIDEBAR_WIDTH);
      sidebar.setMaxWidth(SIDEBAR_WIDTH);
    }
    eventBus.post(new ShowSidebarChangeEvent(showSidebar));
  }

  public void quit() {
    LOG.info("MenuItem quit event");
    Platform.exit();
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
        showEditTagsForm(selectedTrack);
      } catch (Exception e) {
        LOG.error("couldn't load the edit tags form ", e);
      }
    }
  }

  public void onRemoveTrackMenuItem() {
    LOG.info("onRemoveTrackMenuItemClicked");
    removeSelectedTrack();
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onStartPlayingTrackEvent(StartPlayingTrackEvent event) {
    playlistView.getSelectionModel().select(event.track());
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

  private int getSelectedTrackIndex() {
    return playlistView.getSelectionModel().getFocusedIndex();
  }

  private Track getSelectedTrack() {
    return playlistView.getSelectionModel().getSelectedItem();
  }

  private void showEditTagsForm(Track selectedTrack) throws Exception {
    FXMLLoader loader  = new FXMLLoader();
    loader.setLocation(getResource(EDIT_TAGS_FXML));
    Stage stage = new Stage();
    loader.setControllerFactory(clazz -> editTagsControllerFactory(stage, clazz, selectedTrack));
    Scene scene = new Scene(loader.load(), 300, 200);
    stage.setMinWidth(300);
    stage.setMinHeight(200);
    stage.setScene(scene);
    stage.show();
  }

  private Object editTagsControllerFactory(Stage stage, Class<?> clazz, Track track) {
    if (clazz.equals(EditTagsController.class)) {
      return new EditTagsController(stage, track, trackMetadataUpdater, eventBus);
    }
    throw new IllegalArgumentException();
  }

  private void playOrPauseSelectedTrack() {
    eventBus.post(new PlayOrPauseTrackCommand(getSelectedTrack(), getSelectedTrackIndex()));
  }

  private void removeSelectedTrack() {
    var trackIndex = getSelectedTrackIndex();
    if (trackIndex >= 0) {
      eventBus.post(new RemoveTrackCommand(trackIndex));
    }
  }

  private void setCellValueFactories() {
    titleColumn.setCellValueFactory(this::getTitleProperty);
    albumColumn.setCellValueFactory(this::getAlbumProperty);
    artistColumn.setCellValueFactory(this::getArtistProperty);
    lengthColumn.setCellValueFactory(this::getLengthProperty);
    filenameColumn.setCellValueFactory(this::getFileNameProperty);
  }

  private void registerKeyCombinations() {
    openFileMenuItem.setAccelerator(keyCombination(OPEN_FILE_KEY_COMBINATION));
    addTrackMenuItem.setAccelerator(keyCombination(ADD_TRACK_KEY_COMBINATION));
    addDirectoryMenuItem.setAccelerator(keyCombination(ADD_DIRECTORY_KEY_COMBINATION));
    clearPlaylistMenuItem.setAccelerator(keyCombination(CLEAR_PLAYLIST_COMBINATION));
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

  private void addTracksToPlaylist(Collection<Track> tracks) {
    Platform.runLater(() -> {
      tracksQueue.addAll(tracks);
      playlistView.refresh();
    });
  }

  private File playlistAutoSaveFile() {
    return FileUtils.getApplicationDataDirectory().resolve(PLAYLIST_AUTO_SAVE_FILENAME).toFile();
  }

}
