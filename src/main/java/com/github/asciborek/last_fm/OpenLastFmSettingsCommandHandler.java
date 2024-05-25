package com.github.asciborek.last_fm;

import com.github.asciborek.player.OpenLastFmSettingsCommand;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Resources;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class OpenLastFmSettingsCommandHandler {

  @Subscribe
  public void onOpenLastFmSettingsCommand(OpenLastFmSettingsCommand command) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader();
    fxmlLoader.setLocation(Resources.getResource("fxml/last_fm_sign_in.fxml"));
    fxmlLoader.setControllerFactory(_ -> new SignInController());
    Stage stage = new Stage();
    Scene scene = new Scene(fxmlLoader.load(), 300, 200);
    stage.setMinWidth(300);
    stage.setMinHeight(200);
    stage.setScene(scene);
    stage.show();
  }
}
