package com.github.asciborek.metadata;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
public class EditTagsController implements Initializable{

  private static final Logger LOG = LoggerFactory.getLogger(EditTagsController.class);

  private final Stage stage;
  private final TrackMetadataUpdater trackMetadataUpdater;
  private final EventBus eventBus;
  private final Track track;

  @FXML
  private TextField title;
  @FXML
  private TextField album;
  @FXML
  private TextField artist;

  public EditTagsController(Stage stage, Track track, TrackMetadataUpdater trackMetadataUpdater,
      EventBus eventBus) {
    this.stage = stage;
    this.trackMetadataUpdater = trackMetadataUpdater;
    this.track = track;
    this.eventBus = eventBus;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    initTitleField();
    initAlbumField();
    initArtistField();
  }

  public void onCancelButtonClicked(MouseEvent event) {
    LOG.info("onCancelButtonClicked {} ", event);
    stage.close();
  }

  public void onSaveButtonClicked(MouseEvent event) {
    LOG.info("onSaveButtonClicked {} ", event);
    var newTrack = updatedTrackMetadata();
    trackMetadataUpdater.updateTrackMedata(newTrack);
    eventBus.post(new TrackMetadataUpdatedEvent(track, newTrack));
    stage.close();
  }

  private void initArtistField() {
    if (Strings.isNullOrEmpty(track.artist())) {
      artist.setText("");
    } else {
      artist.setText(track.artist());
    }
  }

  private void initAlbumField() {
    if (Strings.isNullOrEmpty(track.album())) {
      album.setText("");
    } else {
      album.setText(track.album());
    }
  }

  private void initTitleField() {
    if (Strings.isNullOrEmpty(track.title())) {
      title.setText("");
    } else {
      title.setText(track.title());
    }
  }

  private Track updatedTrackMetadata() {
    return track.toBuilder()
        .withTitle(title.getText())
        .withAlbum(album.getText())
        .withArtist(artist.getText())
        .build();
  }

}
