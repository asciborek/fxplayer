package com.github.asciborek.player;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioPlayerController {

  private static final Logger LOG = LoggerFactory.getLogger(AudioPlayerController.class);

  private final ObservableList<Track> tracks;
  // UI Fields
  @FXML
  private Label volumeLabel;
  @FXML
  private Slider volumeSlider;

  @Inject
  public AudioPlayerController(ObservableList<Track> tracks) {
    this.tracks = tracks;
  }

  public void changeVolume() {
    volumeLabel.setText(" " + (int)volumeSlider.getValue() + "%");
    LOG.info("slider value: {}", volumeSlider.getValue());
  }

}
