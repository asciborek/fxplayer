package com.github.asciborek.metadata;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.wav.WavTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AudioFileMetadataUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(AudioFileMetadataUpdater.class);
  private final AudioFileExtension supportedFileExtension;

  AudioFileMetadataUpdater(AudioFileExtension supportedFileExtension) {
    this.supportedFileExtension = supportedFileExtension;
  }

  final void updateTrack(Track track) {
    try {
      updateTrackMetadata(track);
      LOG.info("The track {} was updated", track.filePath());
    } catch (Exception e) {
      LOG.error("update {} track metadata error", track, e);
    }
  }

  abstract void updateTrackMetadata(Track track) throws Exception;

  final AudioFileExtension supportedFileExtension() {
    return supportedFileExtension;
  }

}

final class Mp3AudioFileMetadataUpdater extends AudioFileMetadataUpdater {

  Mp3AudioFileMetadataUpdater() {
    super(AudioFileExtension.MP3);
  }

  @Override
  void updateTrackMetadata(Track track) throws Exception {
    var mp3AudioFile = (MP3File) AudioFileIO.read(track.filePath().toFile());
    var tag = mp3AudioFile.getTag();
    tag.setField(FieldKey.TITLE, track.title());
    tag.setField(FieldKey.ALBUM, track.album());
    tag.setField(FieldKey.ARTIST, track.artist());
    mp3AudioFile.commit();
  }
}

final class Mp4AudioFileMetadataUpdater extends AudioFileMetadataUpdater {

  Mp4AudioFileMetadataUpdater() {
    super(AudioFileExtension.MP4);
  }

  @Override
  void updateTrackMetadata(Track track) throws Exception {
    var audioFile = AudioFileIO.read(track.filePath().toFile());
    Mp4Tag mp4tag = (Mp4Tag)audioFile.getTag();
    mp4tag.setField(Mp4FieldKey.TITLE, track.title());
    mp4tag.setField(Mp4FieldKey.ALBUM, track.album());
    mp4tag.setField(Mp4FieldKey.ARTIST, track.artist());
    audioFile.commit();
  }
}

final class WavAudioFileMetadataUpdater extends AudioFileMetadataUpdater {

  WavAudioFileMetadataUpdater() {
    super(AudioFileExtension.WAV);
  }

  @Override
  void updateTrackMetadata(Track track) throws Exception {
    var audioFile = AudioFileIO.read(track.filePath().toFile());
    var wavInfoTag = ((WavTag) audioFile.getTag());
    wavInfoTag.setField(FieldKey.TITLE, track.title());
    wavInfoTag.setField(FieldKey.ALBUM, track.album());
    wavInfoTag.setField(FieldKey.ARTIST, track.artist());
    audioFile.commit();
  }
}