package com.github.asciborek.metadata;

import com.google.common.io.Files;
import java.io.File;
import java.time.Duration;
import java.util.Objects;
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

abstract class AudioFileMetadataProvider {

  private static final Logger LOG = LoggerFactory.getLogger(AudioFileMetadataProvider.class);
  private final AudioFileExtension supportedFileExtension;

  AudioFileMetadataProvider(AudioFileExtension supportedFileExtension) {
    this.supportedFileExtension = supportedFileExtension;
  }

  public final Optional<Track> getTrackMetadata(File file) {
    var extension = Files.getFileExtension(file.toString());
    if (!Objects.equals(AudioFileExtension.valueOfIgnoreCase(extension), supportedFileExtension)) {
      throw new IllegalArgumentException("Unsupported file extension");
    }
    try {
      Track track = readTrackMetadata(file);
      return Optional.of(track);
    } catch (Exception e) {
      LOG.error("load file {} metadata error ", file, e);
      return Optional.empty();
    }
  }

  abstract Track readTrackMetadata(File file) throws Exception;

  final AudioFileExtension supportedFileExtension() {
    return supportedFileExtension;
  }

  static Duration getDuration(AudioFile audioFile) {
    var length = audioFile.getAudioHeader().getTrackLength();
    return Duration.ofSeconds(length);
  }

}

final class Mp3AudioFileMetadataProvider extends AudioFileMetadataProvider {

  Mp3AudioFileMetadataProvider() {
    super(AudioFileExtension.MP3);
  }

  @Override
  Track readTrackMetadata(File mp3File) throws Exception {
    var mp3AudioFile = (MP3File) AudioFileIO.read(mp3File);
    return Track.builder()
        .withTitle(mp3AudioFile.getID3v1Tag().getFirstTitle().trim())
        .withArtist(mp3AudioFile.getID3v1Tag().getFirstArtist().trim())
        .withAlbum(mp3AudioFile.getID3v1Tag().getFirstAlbum().trim())
        .withDuration(getDuration(mp3AudioFile))
        .withFilePath(mp3File.toPath())
        .build();
  }
}

final class Mp4AudioFileMetadataProvider extends AudioFileMetadataProvider {

  Mp4AudioFileMetadataProvider() {
    super(AudioFileExtension.MP4);
  }

  @Override
  Track readTrackMetadata(File mp4File) throws Exception {
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
}

final class WavAudioFileMetadataProvider extends AudioFileMetadataProvider {

  WavAudioFileMetadataProvider() {
    super(AudioFileExtension.WAV);
  }

  @Override
  Track readTrackMetadata(File wavFile) throws Exception {
    var audioFile = AudioFileIO.read(wavFile);
    var wavInfoTag = ((WavTag)audioFile.getTag()).getInfoTag();
    return Track.builder()
        .withTitle(wavInfoTag.getFirst(FieldKey.TITLE).trim())
        .withArtist(wavInfoTag.getFirst(FieldKey.ARTIST).trim())
        .withAlbum(wavInfoTag.getFirst(FieldKey.ALBUM).trim())
        .withDuration(getDuration(audioFile))
        .withFilePath(wavFile.toPath())
        .build();
  }
}