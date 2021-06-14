package com.github.asciborek.util;

import com.github.asciborek.playlist.Track;
import com.google.common.io.Files;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.wav.WavTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MetadataUtils {

  private static final Logger LOG = LoggerFactory.getLogger(MetadataUtils.class);

  private static final String MP_3_EXTENSION = "mp3";
  private static final String MP_4_EXTENSION = "mp4";
  private static final String WAV_EXTENSION = "wav";

  public static Optional<Track> getTrackMetaData(Path musicFile) {
    return getTrackMetaData(musicFile.toFile());
  }

  @SuppressWarnings("UnstableApiUsage")
  public static Optional<Track> getTrackMetaData(File musicFile) {
    var extension = Files.getFileExtension(musicFile.getPath());
    try {
      return switch (extension) {
        case MP_3_EXTENSION -> Optional.of(getMp3MetaData(musicFile));
        case MP_4_EXTENSION -> Optional.of(getMp4MetaData(musicFile));
        case WAV_EXTENSION -> Optional.of(getWavMetaData(musicFile));
        default -> Optional.empty();
      };
    } catch (Exception e) {
      LOG.error("Error loading metadata for file {} .", musicFile.toPath(), e);
      return Optional.empty();
    }
  }

  private static Track getMp3MetaData(File mp3File) throws Exception  {
    var mp3AudioFile = (MP3File) AudioFileIO.read(mp3File);
    return Track.builder()
        .withTitle(mp3AudioFile.getID3v1Tag().getFirstTitle().trim())
        .withArtist(mp3AudioFile.getID3v1Tag().getFirstArtist().trim())
        .withAlbum(mp3AudioFile.getID3v1Tag().getFirstAlbum().trim())
        .withDuration(getDuration(mp3AudioFile))
        .withFilePath(mp3File.toPath())
        .build();
  }

  private static Track getMp4MetaData(File mp4File) throws Exception {
    var audioFile = AudioFileIO.read(mp4File);
    Mp4Tag mp4tag = (Mp4Tag)audioFile.getTag();
    return Track.builder()
        .withTitle(mp4tag.getFirst(Mp4FieldKey.TITLE).trim())
        .withArtist(mp4tag.getFirst(Mp4FieldKey.ARTIST).trim())
        .withAlbum(mp4tag.getFirst(Mp4FieldKey.ALBUM).trim())
        .withDuration(getDuration(audioFile))
        .withFilePath(mp4File.toPath())
        .build();
  }

  private static Track getWavMetaData(File wavFile) throws Exception {
    var audioFile = AudioFileIO.read(wavFile);
    var wavTag = (WavTag)audioFile.getTag();
    return Track.builder()
        .withTitle(wavTag.getFirst(FieldKey.TITLE).trim())
        .withArtist(wavTag.getFirst(FieldKey.ARTIST).trim())
        .withAlbum(wavTag.getFirst(FieldKey.ALBUM).trim())
        .withDuration(getDuration(audioFile))
        .withFilePath(wavFile.toPath())
        .build();
  }

  private static Duration getDuration(AudioFile audioFile) {
    var length = audioFile.getAudioHeader().getTrackLength();
    return Duration.ofSeconds(length);
  }

}
