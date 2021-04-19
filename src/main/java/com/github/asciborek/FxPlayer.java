package com.github.asciborek;

import static com.google.common.io.Resources.getResource;

import com.github.asciborek.player.PlayerModule;
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
  private final Injector injector = Guice.createInjector(new ApplicationModule(), new PlayerModule());

  @Override
  public void init() throws Exception {
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("FxPlayer");
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getResource("fxml/main_window.fxml"));
    loader.setControllerFactory(injector::getInstance);
    Parent root = loader.load();
    primaryStage.setMinHeight(1000);
    primaryStage.setMinWidth(1200);
    primaryStage.setScene(new Scene(root, 1200, 1000));
    primaryStage.setMaximized(true);
    primaryStage.show();
  }

  //The JavaFx "stop" method won't handle SIGINT
  private void shutdown() {
    LOG.info("executing shutdown hook...");
    ExecutorService executorService = injector.getInstance(ExecutorService.class);
    LOG.info("shutting down executorService");
    executorService.shutdownNow();
  }

}
