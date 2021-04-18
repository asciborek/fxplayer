package com.github.asciborek.player;

import com.google.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioPlayerController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(AudioPlayerController.class);

  private final ObservableList<Track> tracks;
  // UI Fields
  @FXML
  private Label volumeLabel;
  @FXML
  private Slider volumeSlider;
  @FXML
  private ProgressBar trackProgress;

  @Inject
  public AudioPlayerController(ObservableList<Track> tracks) {
    this.tracks = tracks;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    trackProgress.setProgress(0);
  }

  public void changeVolume() {
    volumeLabel.setText(" " + (int)volumeSlider.getValue() + "%");
    LOG.info("the slider value: {}", volumeSlider.getValue());
  }

  public void seek(MouseEvent mouseEvent) {
    double selectedValue = (mouseEvent.getX()/ trackProgress.getWidth());
    LOG.info("the track progress bar clicked, the selected value {}", selectedValue);
  }

}
