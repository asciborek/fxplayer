package com.github.asciborek.last_fm;

import com.github.asciborek.last_fm.UserAuthenticationEvent.UserAuthenticatedEvent;
import com.google.common.eventbus.Subscribe;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class LastFmSettingsWindowController implements Initializable {

  private static final String NOT_LOGGED_IN = "You are not logged in...";
  private static final String WAITING_FOR_BROWSERS_CONFIRMATION = "Waiting for your confirmation in browser...";
  private static final String BROWSER_CONFIRMATION_TIMEOUT = "Could not get browser confirmation. \n You can try again";
  private static final String AUTHENTICATION_ERROR = "There was an error while trying to authenticate";
  private final LastFmUserService lastFmUserService;
  private final LastFmAuthenticationHandler lastFmAuthenticationHandler;

  @FXML
  private Text loginStatusText;

  @FXML
  private Button loginButton;

  public LastFmSettingsWindowController(LastFmUserService lastFmUserService,
      LastFmAuthenticationHandler lastFmAuthenticationHandler) {
    this.lastFmUserService = lastFmUserService;
    this.lastFmAuthenticationHandler = lastFmAuthenticationHandler;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    lastFmUserService.getUserSession().ifPresentOrElse(
        this::handleAuthenticatedUsername,
        this::handleNotAuthenticatedUser
    );
  }

  public void onLogin() {
    lastFmAuthenticationHandler.authenticate();
  }

  @Subscribe
  public void onWaitingForBrowsersConfirmation(UserAuthenticationEvent.WaitingForBrowserConfirmationEvent event) {
    loginStatusText.setText(WAITING_FOR_BROWSERS_CONFIRMATION);
    loginButton.setDisable(true);
  }

  @Subscribe
  public void onBrowserConfirmationTimeout(UserAuthenticationEvent.BrowserConfirmationTimeoutEvent timeoutEvent) {
    loginStatusText.setText(BROWSER_CONFIRMATION_TIMEOUT);
    loginButton.setDisable(false);
    loginButton.setVisible(true);
  }

  @Subscribe
  public void onNotRetryableAuthenticationError(UserAuthenticationEvent.NotRetryableAuthenticationErrorEvent event) {
    loginStatusText.setText(AUTHENTICATION_ERROR);
    loginButton.setDisable(false);
    loginButton.setVisible(true);
  }

  @Subscribe
  public void onUserAuthenticatedEvent(UserAuthenticatedEvent userAuthenticatedEvent) {
    handleAuthenticatedUsername(userAuthenticatedEvent.userSession());
  }

  private void handleAuthenticatedUsername(UserSession userSession) {
    loginStatusText.setText("Logged in as " + userSession.username());
    loginButton.setVisible(false);
  }

  private void handleNotAuthenticatedUser() {
    loginStatusText.setText(NOT_LOGGED_IN);
    loginButton.setDisable(false);
    loginButton.setVisible(true);
  }

}
