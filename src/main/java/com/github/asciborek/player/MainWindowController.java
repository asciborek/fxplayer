package com.github.asciborek.player;

import static javafx.scene.input.KeyCombination.keyCombination;

import com.github.asciborek.player.PlayerCommand.AddDirectoryCommand;
import com.github.asciborek.player.PlayerCommand.AddTrackCommand;
import com.github.asciborek.player.PlayerCommand.ClearPlaylistCommand;
import com.github.asciborek.player.PlayerCommand.LoadPlaylistCommand;
import com.github.asciborek.player.PlayerCommand.OpenFileCommand;
import com.github.asciborek.player.PlayerCommand.SavePlaylistCommand;
import com.github.asciborek.player.PlayerCommand.ShufflePlaylistCommand;
import com.github.asciborek.player.PlayerEvent.ShowSidebarChangeEvent;
import com.github.asciborek.settings.SettingsService;
import com.github.asciborek.util.FileUtils;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")//Guava EventBus
public final class MainWindowController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(MainWindowController.class);

  private static final String OPEN_FILE_KEY_COMBINATION = "Ctrl + O";
  private static final String ADD_TRACK_KEY_COMBINATION = "Ctrl + Shift + A";
  private static final String ADD_DIRECTORY_KEY_COMBINATION = "Ctrl + Shift + D";
  private static final String CLEAR_PLAYLIST_COMBINATION = "Ctrl + Shift + Q";

  private static final ExtensionFilter AUDIO_FILES_FILTER = new ExtensionFilter(
      "audio files (mp3, mp4, wav)", List.of("*.mp3", "*.wav", "*.mp4"));
  private static final ExtensionFilter MUSIC_FILTER = new ExtensionFilter(
      "music files", List.of("*.mp3", "*.wav", "*.mp4", "*.plst"));
  private static final ExtensionFilter PLAYLIST_EXTENSION_FILTER = new ExtensionFilter(
      "playlist files (*.plst)", "*.plst");

  private static final int SIDEBAR_WIDTH = 300;
  private static final int HIDDEN_SIDEBAR_WIDTH = 0;

  private final EventBus eventBus;
  private final SettingsService settingsService;

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

  public MainWindowController(EventBus eventBus, SettingsService settingsService) {
    this.eventBus = eventBus;
    this.settingsService = settingsService;
  }

  public void initialize(URL url, ResourceBundle resourceBundle) {
    registerKeyCombinations();
  }

  public void openFile() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(MUSIC_FILTER);
    fileChooser.setInitialDirectory(settingsService.getOpenFileFileChooserInitDirectory());
    var selectedFile = fileChooser.showOpenDialog(new Popup());
    if (selectedFile != null) {
      settingsService.setOpenFileFileChooserInitDirectory(selectedFile.getParentFile());
      eventBus.post(new OpenFileCommand(selectedFile));
    }
  }

  public void addTrack() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(AUDIO_FILES_FILTER);
    fileChooser.setInitialDirectory(settingsService.getAddTrackFileChooserInitDirectory());
    var selectedFile = fileChooser.showOpenDialog(new Popup());
    if (selectedFile != null) {
      settingsService.setAddTrackFileChooserInitDirectory(selectedFile.getParentFile());
      eventBus.post(new AddTrackCommand(selectedFile));
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
      eventBus.post(new AddDirectoryCommand(selectedDirectory));
    }
  }

  public void clearPlaylist() {
    eventBus.post(new ClearPlaylistCommand());
  }

  public void shufflePlaylist() {
   eventBus.post(new ShufflePlaylistCommand());
  }

  public void savePlaylist() {
    var fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(new File(FileUtils.getUserHome()));
    fileChooser.getExtensionFilters().add(PLAYLIST_EXTENSION_FILTER);
    var playlistFile = fileChooser.showSaveDialog(new Popup());
    eventBus.post(new SavePlaylistCommand(playlistFile));
  }

  public void loadPlaylist() {
    var fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(new File(FileUtils.getUserHome()));
    fileChooser.getExtensionFilters().add(PLAYLIST_EXTENSION_FILTER);
    var playlistFile = fileChooser.showOpenDialog(new Popup());
    if (playlistFile.exists()) {
      eventBus.post(new LoadPlaylistCommand(playlistFile));
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

  private void registerKeyCombinations() {
    openFileMenuItem.setAccelerator(keyCombination(OPEN_FILE_KEY_COMBINATION));
    addTrackMenuItem.setAccelerator(keyCombination(ADD_TRACK_KEY_COMBINATION));
    addDirectoryMenuItem.setAccelerator(keyCombination(ADD_DIRECTORY_KEY_COMBINATION));
    clearPlaylistMenuItem.setAccelerator(keyCombination(CLEAR_PLAYLIST_COMBINATION));
  }

}
