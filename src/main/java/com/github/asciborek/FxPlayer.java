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
    primaryStage.setMinHeight(1000);
    primaryStage.setMinWidth(1200);
    primaryStage.setScene(new Scene(root, 1200, 1000));
    primaryStage.setMaximized(true);
    primaryStage.show();
  }

}
