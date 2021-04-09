package com.github.asciborek.player;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MetadataUtils {

  private static final Logger LOG = LoggerFactory.getLogger(MetadataUtils.class);

  public static Optional<Track> getTrackMetaData(Path musicFile) {
    return getTrackMetaData(musicFile.toFile());
  }

  public static Optional<Track> getTrackMetaData(File musicFile) {
    try {
      MP3File f = (MP3File) AudioFileIO.read(musicFile);
      AudioHeader audioHeader = f.getAudioHeader();
      var track = Track.builder()
          .withTitle(f.getID3v1Tag().getFirstTitle())
          .withArtist(f.getID3v1Tag().getFirstArtist())
          .withAlbum(f.getID3v1Tag().getFirstAlbum())
          .withDuration(Duration.of(audioHeader.getTrackLength(), ChronoUnit.SECONDS))
          .withFilePath(musicFile.toPath())
          .build();
      return Optional.of(track);
    } catch (Exception e) {
      LOG.error("Error loading metadata for file {} .", musicFile.toPath(), e);
      return Optional.empty();
    }
  }

}
