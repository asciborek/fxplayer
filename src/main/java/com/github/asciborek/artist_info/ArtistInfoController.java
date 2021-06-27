package com.github.asciborek.artist_info;

import com.github.asciborek.player.PlayerEvents.PlaylistClearedEvent;
import com.github.asciborek.player.PlayerEvents.PlaylistFinishedEvent;
import com.github.asciborek.player.PlayerEvents.StartPlayingTrackEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
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
  private String currentArtist = NONE_ARTIST;
  private final ArtistInfoProvider artistInfoProvider;

  @FXML
  private TextArea artistDescription;

  @FXML
  private TextArea similarArtists;

  @Inject
  public ArtistInfoController(ArtistInfoProvider artistInfoProvider, EventBus eventBus) {
    this.artistInfoProvider = artistInfoProvider;
    eventBus.register(this);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onNewTrack(StartPlayingTrackEvent startPlayingTrackEvent) {
    LOG.info("received StartPlayingTrackEvent");
    var newArtist = startPlayingTrackEvent.track().artist();
    if (!newArtist.equals(currentArtist)) {
      currentArtist = newArtist;
      artistInfoProvider
          .getArtistInfo(currentArtist)
          .exceptionally(ex -> ArtistInfo.UNREACHABLE)
          .thenAccept(this::loadArtistInfo);
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlaylistFinished(PlaylistFinishedEvent event) {
    LOG.info("received PlaylistFinishedEvent");
    clearArtistInfo();
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onPlaylistCleared(PlaylistClearedEvent event) {
    LOG.info("received PlaylistClearedEvent");
    clearArtistInfo();
  }

  private void clearArtistInfo() {
    artistDescription.setText("");
    similarArtists.setText("");
  }

  private void loadArtistInfo(ArtistInfo artistInfo) {
    Platform.runLater(() ->{
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
