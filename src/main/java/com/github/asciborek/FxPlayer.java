package com.github.asciborek;

import static com.google.common.io.Resources.getResource;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class FxPlayer extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("FxPlayer");
    Parent root = FXMLLoader.load(getResource("player.fxml"));
    primaryStage.setScene(new Scene(root, 300, 300));
    primaryStage.show();
  }
}
