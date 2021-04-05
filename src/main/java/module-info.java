module fxplayer {
  requires javafx.controls;
  requires javafx.graphics;
  requires javafx.fxml;
  requires javafx.media;
  requires logback.core;
  requires org.slf4j;
  requires com.google.common;
  opens com.github.asciborek to javafx.fxml, javafx.graphics;
  opens com.github.asciborek.player to javafx.fxml, javafx.graphics;
}