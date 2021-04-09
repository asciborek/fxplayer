package com.github.asciborek.player;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerController implements Initializable{
  private static final Logger LOG = LoggerFactory.getLogger(PlayerController.class);

  private final ObservableList<Track> playlist = FXCollections.observableArrayList();

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

  public void initialize(URL url, ResourceBundle resourceBundle) {
    playlistView.setItems(playlist);
    setCellValueFactories();
  }

  public void clearPlaylist(ActionEvent actionEvent) {
    playlist.clear();
  }

  public void quit(ActionEvent actionEvent) {
    LOG.info("MenuItem quit event");
    Platform.exit();
    System.exit(0);
  }

  private void setCellValueFactories() {
    titleColumn.setCellValueFactory(this::getTitleProperty);
    albumColumn.setCellValueFactory(this::getAlbumProperty);
    artistColumn.setCellValueFactory(this::getArtistProperty);
    lengthColumn.setCellValueFactory(this::getLengthProperty);
    filenameColumn.setCellValueFactory(this::getFileNameProperty);
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

}
