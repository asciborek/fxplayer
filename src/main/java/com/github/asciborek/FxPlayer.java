package com.github.asciborek;

import static com.google.common.io.Resources.getResource;

import com.github.asciborek.util.FileUtils;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FxPlayer extends Application {

  private static final Logger LOG = LoggerFactory.getLogger(FxPlayer.class);
  private static final String LAST_FM_PROPERTIES_FILE_NAME = "last_fm.properties";

  private final Properties apiProperties = readApiProperties();
  private final Injector injector = Guice.createInjector(new ApplicationModule(), new LastFmModule(apiProperties));
  private final EventBus eventBus = injector.getInstance(EventBus.class);

  static {
    Path applicationDirectory = FileUtils.getApplicationDataDirectory();
    if (Files.notExists(applicationDirectory)) {
      try {
        Files.createDirectory(applicationDirectory);
      } catch (IOException e) {
        LOG.error("couldn't create application directory");
        throw new UncheckedIOException(e);
      }
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void init() {
    //The JavaFx "stop" method won't handle SIGINT
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownExecutor));
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    var updater = new WindowTitleUpdater(primaryStage);
    eventBus.register(updater);

    primaryStage.setTitle(WindowTitleUpdater.DEFAULT_TITLE);
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getResource("fxml/main_window.fxml"));
    loader.setControllerFactory(injector::getInstance);
    Parent root = loader.load();

    initMainWindow(primaryStage, root);
  }

  @Override
  public void stop() {
    LOG.info("Application Exit");
    eventBus.post(new CloseApplicationEvent());
    new Thread(this::shutdownExecutor).start(); //to prevent frozen UI
  }

  private void initMainWindow(Stage primaryStage, Parent root) {
    primaryStage.setMinHeight(1000);
    primaryStage.setMinWidth(1200);
    primaryStage.setScene(new Scene(root, 1200, 1000));
    primaryStage.setMaximized(true);
    primaryStage.show();
  }

  private Properties readApiProperties() {
    var properties = new Properties();
    try (InputStream inputStream = Resources.getResource(LAST_FM_PROPERTIES_FILE_NAME).openStream()) {
      properties.load(inputStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return properties;
  }

  private void shutdownExecutor() {
    ExecutorService executorService = injector.getInstance(ExecutorService.class);
    if (!executorService.isTerminated()) {
      LOG.info("start terminating executorService");
      try {
        if (!executorService.awaitTermination(3, TimeUnit.SECONDS)){
          LOG.info("awaitTermination didn't terminate executorService, calling executorService.shutdownNow()");
          executorService.shutdownNow();
        }
      } catch (InterruptedException e) {
        LOG.error("InterruptedException during awaitTermination, calling executorService.shutdownNow()", e);
        executorService.shutdownNow();
      }

    }
  }

  public record CloseApplicationEvent() {}

}
