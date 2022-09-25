package com.github.asciborek.player;

import static com.google.common.io.Resources.getResource;

import com.github.asciborek.metadata.EditTagsController;
import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataUpdater;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

final class EditTrackPopupFactory {

  private static final String EDIT_TAGS_FXML = "fxml/edit_tags.fxml";

  private final TrackMetadataUpdater trackMetadataUpdater;
  private final EventBus eventBus;

  EditTrackPopupFactory(TrackMetadataUpdater trackMetadataUpdater, EventBus eventBus) {
    this.trackMetadataUpdater = trackMetadataUpdater;
    this.eventBus = eventBus;
  }

  Stage createPopupForm(Track selectedTrack) throws IOException {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getResource(EDIT_TAGS_FXML));
    Stage stage = new Stage();
    loader.setControllerFactory(clazz -> editTagsControllerFactory(stage, clazz, selectedTrack));
    Scene scene = new Scene(loader.load(), 300, 200);
    stage.setMinWidth(300);
    stage.setMinHeight(200);
    stage.setScene(scene);
    return stage;
  }

  private Object editTagsControllerFactory(Stage stage, Class<?> clazz, Track track) {
    if (clazz.equals(EditTagsController.class)) {
      return new EditTagsController(stage, track, trackMetadataUpdater, eventBus);
    }
    throw new IllegalArgumentException();
  }
}
