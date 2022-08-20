package com.github.asciborek.playlist;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.asciborek.metadata.Track;
import com.github.asciborek.metadata.TrackMetadataProvider;
import com.google.common.io.Resources;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@SuppressWarnings("UnstableApiUsage")
class PlaylistServiceTest {
  private final List<String> supportedAudioExtensions = List.of("mp3", "mp4", "wav");
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final PlaylistService playlistService = getPlaylistService();

  private static final String ARTIST_NAME = "aciborek";
  private static final String ALBUM_NAME = "javafx";
  private static final String TRACK_TITLE = "dummy";
  private static final Duration TRACK_DURATION  = Duration.of(30, ChronoUnit.SECONDS);

  @Test
  @DisplayName("load all audio files from a directory")
  void loadAllAudioFilesFromDirectory() throws Exception {
    var future = playlistService.getDirectoryTracks(getPathFromResource().toFile());
    var result = future.get(5, TimeUnit.SECONDS);
    assertThat(result.size()).isEqualTo(3);
    assertThat(result.get(0).fileName()).isEqualTo("1_test_audio.mp3");
    assertThat(result.get(1).fileName()).isEqualTo("2_test_audio.mp4");
    assertThat(result.get(2).fileName()).isEqualTo("3_test_audio.wav");
  }

  @Test
  @DisplayName("write and load tracks from the playlist file")
  void writeAndLoadTracksFromPlaylistFile() throws Exception {
    //Prepare Data
    var firstExistingFilePath = getAudioFilePath("1_test_audio.mp3");
    var secondExistingFilePath = getAudioFilePath("2_test_audio.mp4");
    var notExistingFilePath = getAudioFilePath("404.mp3");
    var playlistToSave = List.of(testTrack(firstExistingFilePath),
        testTrack(secondExistingFilePath), testTrack(notExistingFilePath));
    var playlistFile = File.createTempFile("playlist", ".plst");
    var expectedPlaylist = List.of(testTrack(firstExistingFilePath),
        testTrack(secondExistingFilePath));
    //Save and load
    playlistService.savePlaylist(playlistFile, playlistToSave);
    var loadedPlaylist = playlistService.loadPlaylistWithExistingFiles(playlistFile).get(3, TimeUnit.SECONDS);
    //Assert
    assertThat(loadedPlaylist).isEqualTo(expectedPlaylist);
  }

  private Path getAudioFilePath(String file) {
    return getPathFromResource().resolve("audio").resolve(file);
  }

  @AfterAll
  void closeExecutor() {
    executorService.shutdownNow();
  }

  private Track testTrack(Path filePath) {
    return Track.builder()
        .withArtist(ARTIST_NAME)
        .withAlbum(ALBUM_NAME)
        .withTitle(TRACK_TITLE)
        .withDuration(TRACK_DURATION)
        .withFilePath(filePath)
        .build();
  }

  private PlaylistService getPlaylistService() {
    return new PlaylistService(executorService, new TrackMetadataProvider(), playlistStorage(), supportedAudioExtensions);
  }

  private PlaylistStorage playlistStorage() {
    var mapper = new XmlMapper();
    mapper.registerModules(new Jdk8Module());
    mapper.registerModules(new JavaTimeModule());
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return new XmlPlaylistStorage(mapper);
  }

  private Path getPathFromResource() {
    try {
      return Path.of(Resources.getResource("data").toURI());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
