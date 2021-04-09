package com.github.asciborek;

import static com.google.common.io.Resources.getResource;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.concurrent.ExecutorService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FxPlayer extends Application {

  private static final Logger LOG = LoggerFactory.getLogger(FxPlayer.class);
  private final Injector injector = Guice.createInjector(new ApplicationModule());

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("FxPlayer");
    FXMLLoader loader = new FXMLLoader();
    loader.setControllerFactory(injector::getInstance);
    Parent root = loader.load(getResource("player.fxml").openStream());
    primaryStage.setMinHeight(1000);
    primaryStage.setMinWidth(1200);
    primaryStage.setScene(new Scene(root, 1200, 1000));
    primaryStage.setMaximized(true);
    primaryStage.show();
  }

  @Override
  public void stop() throws Exception {
    LOG.info("shutting down application...");
    ExecutorService executorService = injector.getInstance(ExecutorService.class);
    LOG.info("shutting down executorService");
    executorService.shutdownNow();
  }
}
