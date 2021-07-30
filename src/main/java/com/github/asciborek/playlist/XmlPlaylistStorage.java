package com.github.asciborek.playlist;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.asciborek.metadata.Track;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class XmlPlaylistStorage implements PlaylistStorage {
  private static final Logger LOG = LoggerFactory.getLogger(XmlPlaylistStorage.class);

  private final XmlMapper xmlMapper;

  public XmlPlaylistStorage(XmlMapper xmlMapper) {
    this.xmlMapper = xmlMapper;
  }

  @Override
  public void savePlaylist(Path playlistFilePath, List<Track> playlist) {
    try (var writer = Files.newBufferedWriter(playlistFilePath)) {
      var items = playlist.stream()
          .map(this::fromTrack)
          .toList();
      xmlMapper.writeValue(writer, new Playlist(items));
    } catch (Exception e) {
      LOG.error("savePlaylist error", e);
    }
  }

  @Override
  public List<Track> loadPlaylist(Path playlistFilePath) {
    if (!Files.exists(playlistFilePath)) {
      LOG.info("playlist file {} does not exist, return an empty playlist", playlistFilePath);
      return List.of();
    }
    try (var reader = Files.newBufferedReader(playlistFilePath)) {
      return xmlMapper.readValue(reader, Playlist.class).tracks()
          .stream()
          .map(this::fromXmlItem)
          .toList();
    } catch (Exception e) {
      LOG.error("Read playlist file {} error",  playlistFilePath, e);
      return List.of();
    }
  }

  private PlaylistItem fromTrack(Track track) {
    return new PlaylistItem(track.title(), track.album(), track.artist(), track.duration(),
        track.filePath().toFile().toString());
  }

  private Track fromXmlItem(PlaylistItem playlistItem) {
    return Track.builder()
        .withTitle(playlistItem.title())
        .withAlbum(playlistItem.album())
        .withArtist(playlistItem.artist())
        .withDuration(playlistItem.duration())
        .withFilePath(Path.of(playlistItem.filePath()))
        .build();
  }

}
