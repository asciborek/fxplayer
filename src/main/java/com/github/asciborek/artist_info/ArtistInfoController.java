package com.github.asciborek.artist_info;

import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataUpdatedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistClearedEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvent.ShowSidebarChangeEvent;
import com.github.asciborek.player.PlayerEvent.StartPlayingTrackEvent;
import com.google.common.eventbus.Subscribe;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")//Guava EventBus
public class ArtistInfoController {

  private static final Logger LOG = LoggerFactory.getLogger(ArtistInfoController.class);
  private static final String NONE_ARTIST = "";

  private final ArtistInfoProvider artistInfoProvider;

  private String currentArtist = NONE_ARTIST;
  private Track currentTrack = null;
  private boolean showSidebar = true;

  @FXML
  private TextArea artistDescription;

  @FXML
  private TextArea similarArtists;

  ArtistInfoController(ArtistInfoProvider artistInfoProvider) {
    this.artistInfoProvider = artistInfoProvider;
  }

  @Subscribe
  public void onShowSidebarChange(ShowSidebarChangeEvent event) {
    boolean previousSHowSidebar = showSidebar;
    showSidebar = event.showSidebar();
    LOG.info("received showSidebarChangeEvent {}", event);
    if (!previousSHowSidebar && showSidebar) {
      loadArtistInfo();
    }
  }

  @Subscribe
  public void onNewTrack(StartPlayingTrackEvent startPlayingTrackEvent) {
    LOG.info("received StartPlayingTrackEvent");
    currentTrack = startPlayingTrackEvent.track();
    var newArtist = currentTrack.artist();
    if (!newArtist.equals(currentArtist)) {
      currentArtist = newArtist;
      if (showSidebar) {
        loadArtistInfo();
      }
    }
  }

  @Subscribe
  public void onTrackMetadataUpdatedEvent(TrackMetadataUpdatedEvent event) {
    LOG.info("received TrackMetadataUpdatedEvent");
    if ((Objects.equals(currentTrack, event.oldTrack())) && !(Objects.equals(currentTrack, event.newTrack()))) {
      currentTrack = event.newTrack();
      if (!currentArtist.equals(event.newTrack().artist())) {
        this.currentArtist = event.newTrack().artist();
        if (showSidebar) {
          loadArtistInfo();
        }
      }
    }
  }

  private void loadArtistInfo() {
    artistInfoProvider
        .getArtistInfo(currentArtist)
        .exceptionally(ex -> ArtistInfo.UNREACHABLE)
        .thenAccept(this::loadArtistInfo);
  }

  @Subscribe
  public void onPlaylistFinished(PlaylistFinishedEvent event) {
    LOG.info("received PlaylistFinishedEvent");
    clearArtistInfo();
    this.currentTrack = null;
  }

  @Subscribe
  public void onPlaylistCleared(PlaylistClearedEvent event) {
    LOG.info("received PlaylistClearedEvent");
    this.currentTrack = null;
    clearArtistInfo();
  }

  private void clearArtistInfo() {
    artistDescription.setText("");
    similarArtists.setText("");
  }

  private void loadArtistInfo(ArtistInfo artistInfo) {
    Platform.runLater(() -> {
      artistDescription.setText(artistInfo.description());
      similarArtists.setText(similarArtistsText(artistInfo));
    });
  }

  private String similarArtistsText(ArtistInfo artistInfo) {
    var similarArtists = artistInfo.similarArtist();
    if (similarArtists.isEmpty()) {
      return "";
    } else {
      return "Similar artists: " + similarArtists.stream()
          .limit(5)
          .collect(Collectors.joining("\n"));
    }
  }
}
