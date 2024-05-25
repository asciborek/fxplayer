package com.github.asciborek.last_fm;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignInController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(SignInController.class);

  @FXML
  private Button signIn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void onSignIn() {
    LOG.info("signIn button clicked");
  }

}
