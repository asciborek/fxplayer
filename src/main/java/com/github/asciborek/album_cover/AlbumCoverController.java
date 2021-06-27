package com.github.asciborek.album_cover;

import static com.google.common.io.Resources.getResource;

import com.github.asciborek.player.PlayerEvents.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvents.StartPlayingTrackEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")//Guava
public class AlbumCoverController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(AlbumCoverController.class);
  private static final Image EMPTY_CD_IMAGE = new Image(getResource("images/empty_cd.jpg").toString());
  private static final ArtistAlbum NONE = new ArtistAlbum("", "");
  private final AlbumCoverProvider albumCoverProvider;

  @FXML
  private ImageView albumCoverImageView;

  private ArtistAlbum currentArtistAlbum = NONE;

  @Inject
  public AlbumCoverController(AlbumCoverProvider albumCoverProvider, EventBus eventBus) {
    this.albumCoverProvider = albumCoverProvider;
    eventBus.register(this);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    albumCoverImageView.setImage(EMPTY_CD_IMAGE);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onNewTrack(StartPlayingTrackEvent event) {
    LOG.info("received StartPlayingTrackEvent");
    var track = event.track();
    var newTrackArtistAlbum = new ArtistAlbum(track.artist(), track.album());
    if (!Objects.equals(newTrackArtistAlbum, currentArtistAlbum)) {
      currentArtistAlbum = newTrackArtistAlbum;
      albumCoverProvider.fetchAlbum(newTrackArtistAlbum)
          .exceptionally(e -> EMPTY_CD_IMAGE)
          .thenAccept(this::loadCover);
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlaylistFinished(PlaylistFinishedEvent event) {
    LOG.info("received PlaylistFinishedEvent");
    albumCoverImageView.setImage(EMPTY_CD_IMAGE);
  }

  private void loadCover(Image image) {
    Platform.runLater(() -> albumCoverImageView.setImage(image));
  }

}
