package com.github.asciborek.playlist;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.asciborek.metadata.TrackMetadataProvider;
import com.github.asciborek.util.FileUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.concurrent.ExecutorService;

public final class PlaylistServiceFactory implements Provider<PlaylistService> {

  private final ExecutorService executorService;
  private final TrackMetadataProvider trackMetadataProvider;
  private final XmlMapper xmlMapper;

  @Inject
  public PlaylistServiceFactory(ExecutorService executorService,
      TrackMetadataProvider trackMetadataProvider,
      XmlMapper xmlMapper) {
    this.executorService = executorService;
    this.trackMetadataProvider = trackMetadataProvider;
    this.xmlMapper = xmlMapper;
  }

  @Override
  public PlaylistService get() {
    return new PlaylistService( executorService, trackMetadataProvider,
        new XmlPlaylistStorage(xmlMapper), FileUtils.getSupportedAudioFilesExtensions());
  }

}
