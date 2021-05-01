package com.github.asciborek.player;

import com.github.asciborek.player.event.PlayOrPauseTrackCommand;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")//Guava EventBus
public class MainWindowController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(MainWindowController.class);
  private static final String EXTENSION_PREFIX = "*";
  private static final String ADD_SONG_KEY_COMBINATION = "Ctrl + Shift + A";
  private static final String CLEAR_PLAYLIST_COMBINATION = "Ctrl + Shift + Q";
  private final ExtensionFilter supportedFilesExtensionFilter = new ExtensionFilter("audio files", fileChooserExtensions());

  private final EventBus eventBus;
  private final PlaylistService playlistService;
  private final ObservableList<Track> playlist;


  // UI Fields
  @FXML
  private MenuItem addTrackMenuItem;
  @FXML
  private MenuItem clearPlaylistMenuItem;

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
  public MainWindowController(PlaylistService playlistService, EventBus eventBus, ObservableList<Track> playlist) {
    this.playlistService = playlistService;
    this.eventBus = eventBus;
    this.playlist = playlist;
    eventBus.register(this);
  }

  public void initialize(URL url, ResourceBundle resourceBundle) {
    playlistView.setItems(playlist);
    setCellValueFactories();
    registerKeyCombinations();
  }

  public void addTrack() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(supportedFilesExtensionFilter);
    var selectedFile = fileChooser.showOpenDialog(new Popup());
    if (selectedFile != null) {
      playlistService.getTrack(selectedFile)
          .ifPresent(playlist::add);
    }
  }

  public void addDirectory() {
    var directoryChooser = new DirectoryChooser();
    var selectedDirectory = directoryChooser.showDialog(new Popup());
    if (selectedDirectory != null) {
      playlistService.getDirectoryTracks(selectedDirectory)
          .thenAccept(this::addTracksToPlaylist);
    }
  }

  public void clearPlaylist() {
    LOG.info("Clear playlist. Removed items size: {}", playlist.size());
    playlist.clear();
  }

  public void quit() {
    LOG.info("MenuItem quit event");
    System.exit(0);
  }

  public void onPlaylistMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getClickCount() == 2) {
      eventBus.post(new PlayOrPauseTrackCommand(getSelectedTrack()));
    }
  }

  public void onPlaylistKeyClicked(KeyEvent keyEvent) {
    if (keyEvent.getCode() == KeyCode.SPACE) {
      eventBus.post(new PlayOrPauseTrackCommand(getSelectedTrack()));
    }
  }

  private Track getSelectedTrack() {
    return playlistView.getSelectionModel().getSelectedItem();
  }

  private void setCellValueFactories() {
    titleColumn.setCellValueFactory(this::getTitleProperty);
    albumColumn.setCellValueFactory(this::getAlbumProperty);
    artistColumn.setCellValueFactory(this::getArtistProperty);
    lengthColumn.setCellValueFactory(this::getLengthProperty);
    filenameColumn.setCellValueFactory(this::getFileNameProperty);
  }

  private void registerKeyCombinations() {
    addTrackMenuItem.setAccelerator(KeyCombination.keyCombination(ADD_SONG_KEY_COMBINATION));
    clearPlaylistMenuItem.setAccelerator(KeyCombination.keyCombination(CLEAR_PLAYLIST_COMBINATION));
  }

  private StringProperty getTitleProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().getTitle());
  }

  private StringProperty getAlbumProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().getAlbum());
  }

  private StringProperty getArtistProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().getArtist());
  }

  private StringProperty getLengthProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().getLength());
  }

  private StringProperty getFileNameProperty(CellDataFeatures<Track, String> cellData) {
    return new SimpleStringProperty(cellData.getValue().getFileName());
  }

  private List<String> fileChooserExtensions() {
    return FileExtension.getSupportedExtensions().stream()
        .map(ext -> EXTENSION_PREFIX + ext)
        .collect(Collectors.toUnmodifiableList());
  }

  private void addTracksToPlaylist(Collection<Track> tracks) {
    Platform.runLater(() ->{
      playlist.addAll(tracks);
      playlistView.refresh();
    });

  }

}
