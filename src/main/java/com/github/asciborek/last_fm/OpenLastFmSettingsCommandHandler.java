package com.github.asciborek.last_fm;

import static com.google.common.io.Resources.getResource;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class OpenLastFmSettingsCommandHandler {

  private static final Logger LOG = LoggerFactory.getLogger(OpenLastFmSettingsCommandHandler.class);
  private static final String LASTFM_SETTINGS_FXML = "fxml/lastfm_settings.fxml";
  private final LastFmAuthenticationHandler lastFmAuthenticationHandler;
  private final LastFmUserService lastFmUserService;
  private final EventBus eventBus;

  public OpenLastFmSettingsCommandHandler(LastFmAuthenticationHandler lastFmAuthenticationHandler,
      LastFmUserService lastFmUserService, EventBus eventBus) {
    this.lastFmAuthenticationHandler = lastFmAuthenticationHandler;
    this.lastFmUserService = lastFmUserService;
    this.eventBus = eventBus;

  }

  @Subscribe
  public void onOpenLastFmSettingsCommand(OpenLastFmSettingsCommand openLastFmSettingsCommand) throws Exception {
    LOG.info("OpenLastFmSettingsCommand received, opening Last.fm settings window");
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getResource(LASTFM_SETTINGS_FXML));
    Stage stage = new Stage();
    loader.setControllerFactory(this::lastFmSettingsControllerFactory);
    Scene scene = new Scene(loader.load(), 300, 200);
    stage.setMinWidth(300);
    stage.setMinHeight(200);
    stage.setScene(scene);
    stage.show();
  }

  private Object lastFmSettingsControllerFactory(Class<?> clazz) {
    if (clazz.equals(LastFmSettingsWindowController.class)) {
      LastFmSettingsWindowController controller = new LastFmSettingsWindowController(lastFmUserService, lastFmAuthenticationHandler);
      eventBus.register(controller);
      return controller;
    }
    throw new IllegalArgumentException();
  }
}
