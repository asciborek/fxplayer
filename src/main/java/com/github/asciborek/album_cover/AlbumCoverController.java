package com.github.asciborek.album_cover;

import static com.google.common.io.Resources.getResource;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataUpdatedEvent;
import com.github.asciborek.player.PlayerEvents.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvents.ShowSidebarChangeEvent;
import com.github.asciborek.player.PlayerEvents.StartPlayingTrackEvent;
import com.google.common.eventbus.Subscribe;
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
public final class AlbumCoverController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(AlbumCoverController.class);
  private static final Image EMPTY_CD_IMAGE = new Image(getResource("images/empty_cd.jpg").toString());
  private static final ArtistAlbum NONE = new ArtistAlbum("", "");
  private final AlbumCoverProvider albumCoverProvider;

  @FXML
  private ImageView albumCoverImageView;

  private Track currentTrack = null;
  private ArtistAlbum currentArtistAlbum = NONE;
  private boolean showSidebar = true;

  AlbumCoverController(AlbumCoverProvider albumCoverProvider) {
    this.albumCoverProvider = albumCoverProvider;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    albumCoverImageView.setImage(EMPTY_CD_IMAGE);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onShowSidebarChange(ShowSidebarChangeEvent event) {
    boolean previousShowSidebar = showSidebar;
    showSidebar = event.showSidebar();
    LOG.info("received showSidebarChangeEvent {}", event);
    if (!previousShowSidebar && showSidebar) {
      loadAlbumCover();
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onNewTrack(StartPlayingTrackEvent event) {
    LOG.info("received StartPlayingTrackEvent");
    currentTrack = event.track();
    var newTrackArtistAlbum = new ArtistAlbum(currentTrack.artist(), currentTrack.album());
    if (!Objects.equals(newTrackArtistAlbum, currentArtistAlbum)) {
      currentArtistAlbum = newTrackArtistAlbum;
      if (showSidebar) {
        loadAlbumCover();
      }
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onTrackMetadataUpdatedEvent(TrackMetadataUpdatedEvent event) {
    LOG.info("received TrackMetadataUpdatedEvent");
    if (Objects.equals(currentTrack, event.oldTrack())) {
      var oldTrackArtistAlbum = new ArtistAlbum(event.oldTrack().artist(), event.oldTrack().album());
      var newTrackArtistAlbum = new ArtistAlbum(event.newTrack().artist(), event.newTrack().album());
      currentTrack = event.newTrack();
      if (!oldTrackArtistAlbum.equals(newTrackArtistAlbum)) {
        currentArtistAlbum = newTrackArtistAlbum;
        if (showSidebar) {
          loadAlbumCover();
        }
      }
    }
  }

  private void loadAlbumCover() {
    albumCoverProvider.fetchAlbum(currentArtistAlbum)
        .exceptionally(e -> EMPTY_CD_IMAGE)
        .thenAccept(this::loadCover);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlaylistFinished(PlaylistFinishedEvent event) {
    LOG.info("received PlaylistFinishedEvent");
    albumCoverImageView.setImage(EMPTY_CD_IMAGE);
    currentTrack = null;
  }

  private void loadCover(Image image) {
    Platform.runLater(() -> albumCoverImageView.setImage(image));
  }

}
