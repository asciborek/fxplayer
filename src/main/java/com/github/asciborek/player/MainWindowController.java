package com.github.asciborek.player;

import static javafx.scene.input.KeyCombination.keyCombination;

import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.player.command.OpenTrackFileCommand;
import com.github.asciborek.player.command.PlayOrPauseTrackCommand;
import com.github.asciborek.player.event.StartPlayingTrackEvent;
import com.github.asciborek.playlist.PlaylistService;
import com.github.asciborek.playlist.Track;
import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.util.FileUtils;
import com.github.asciborek.util.MetadataUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")//Guava EventBus
public final class MainWindowController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(MainWindowController.class);
  private static final String PLAYLIST_AUTO_SAVE_FILENAME = "playlist_auto_save.plst";
  private static final List<String> AUDIO_FILE_EXTENSIONS = List.of("*.mp3");
  private static final String OPEN_FILE_KEY_COMBINATION = "Ctrl + O";
  private static final String ADD_TRACK_KEY_COMBINATION = "Ctrl + Shift + A";
  private static final String ADD_DIRECTORY_KEY_COMBINATION = "Ctrl + Shift + D";
  private static final String CLEAR_PLAYLIST_COMBINATION = "Ctrl + Shift + Q";
  private static final ExtensionFilter PLAYLIST_EXTENSION_FILTER =
      new ExtensionFilter("playlist files (*.plst)", "*.plst" );

  private final EventBus eventBus;
  private final PlaylistService playlistService;
  private final SettingsService settingsService;
  private final ObservableList<Track> playlist;

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
  public MainWindowController(EventBus eventBus, PlaylistService playlistService, SettingsService settingsService,  ObservableList<Track> playlist) {
    this.playlistService = playlistService;
    this.eventBus = eventBus;
    this.settingsService = settingsService;
    this.playlist = playlist;
    eventBus.register(this);
  }

  public void initialize(URL url, ResourceBundle resourceBundle) {
    playlistService.loadPlaylistWithExistingFiles(playlistAutoSaveFile()).
        thenAccept(this::addTracksToPlaylist);
    playlistView.setItems(playlist);
    setCellValueFactories();
    registerKeyCombinations();
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onCloseExit(CloseApplicationEvent closeApplicationEvent) {
    LOG.info("save {} track(s) to the auto-save playlist", playlist.size());
    playlistService.savePlaylist(playlistAutoSaveFile(), ImmutableList.copyOf(playlist));
  }

  public void openFile() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new ExtensionFilter("audio files", AUDIO_FILE_EXTENSIONS));
    fileChooser.setInitialDirectory(settingsService.getOpenFileFileChooserInitDirectory());
    var selectedFile = fileChooser.showOpenDialog(new Popup());
    if (selectedFile != null) {
      settingsService.setOpenFileFileChooserInitDirectory(selectedFile.getParentFile());
      MetadataUtils.getTrackMetaData(selectedFile)
          .ifPresent(this::onOpenFile);
    }
  }

  private void onOpenFile(Track track) {
    playlist.clear();
    playlist.add(track);
    eventBus.post(new OpenTrackFileCommand(track));
  }

  public void addTrack() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new ExtensionFilter("audio files", AUDIO_FILE_EXTENSIONS));
    fileChooser.setInitialDirectory(settingsService.getAddTrackFileChooserInitDirectory());
    var selectedFile = fileChooser.showOpenDialog(new Popup());
    if (selectedFile != null) {
      settingsService.setAddTrackFileChooserInitDirectory(selectedFile.getParentFile());
      playlistService.getTrack(selectedFile)
          .ifPresent(playlist::add);
    }
  }

  public void addDirectory() {
    var directoryChooser = new DirectoryChooser();
    directoryChooser.setInitialDirectory(settingsService.getDirectoryDirectoryChooserInitDirectory());
    var selectedDirectory = directoryChooser.showDialog(new Popup());
    if (selectedDirectory != null) {
      settingsService.setAddDirectoryDirectoryChooserInitDirectory(selectedDirectory.getParentFile());
      playlistService.getDirectoryTracks(selectedDirectory)
          .thenAccept(this::addTracksToPlaylist);
    }
  }

  public void clearPlaylist() {
    LOG.info("Clear playlist. Removed items size: {}", playlist.size());
    playlist.clear();
  }

  public void savePlaylist() {
    var playlistToSave = ImmutableList.copyOf(playlist);
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
      playlistService.loadPlaylistWithExistingFiles(playlistFile).thenAccept(this::addTracksToPlaylist);
    }
  }

  public void quit() {
    LOG.info("MenuItem quit event");
    Platform.exit();
  }

  public void onPlaylistMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getClickCount() == 2) {
      var selectedTrack = getSelectedTrack();
      LOG.info("onPlaylistMouseClicked selectedTrack: {}", selectedTrack);
      eventBus.post(new PlayOrPauseTrackCommand(selectedTrack));
    }
  }

  public void onPlaylistKeyClicked(KeyEvent keyEvent) {
    switch (keyEvent.getCode()) {
      case SPACE -> playOrPauseSelectedTrack();
      case DELETE -> removeSelectedTrack();
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onStartPlayingTrackEvent(StartPlayingTrackEvent event) {
    playlistView.getSelectionModel().select(event.track());
  }

  private Track getSelectedTrack() {
    return playlistView.getSelectionModel().getSelectedItem();
  }

  private void playOrPauseSelectedTrack() {
    eventBus.post(new PlayOrPauseTrackCommand(getSelectedTrack()));
  }

  private void removeSelectedTrack(){
    playlist.remove(getSelectedTrack());
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
    Platform.runLater(() ->{
      playlist.addAll(tracks);
      playlistView.refresh();
    });
  }

  private File playlistAutoSaveFile() {
    return FileUtils.getApplicationDataDirectory().resolve(PLAYLIST_AUTO_SAVE_FILENAME).toFile();
  }

}
