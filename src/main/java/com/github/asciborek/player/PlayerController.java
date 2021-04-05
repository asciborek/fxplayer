package com.github.asciborek.player;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerController {
  private static final Logger LOG = LoggerFactory.getLogger(PlayerController.class);

  public void quit(ActionEvent actionEvent) {
    LOG.info("MenuItem quit event");
    Platform.exit();
    System.exit(0);
  }

}
