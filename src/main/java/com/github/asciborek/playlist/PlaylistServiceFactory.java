package com.github.asciborek.playlist;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.asciborek.util.FileUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.concurrent.ExecutorService;

public final class PlaylistServiceFactory implements Provider<PlaylistService> {

  private final ExecutorService executorService;

  @Inject
  public PlaylistServiceFactory(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public PlaylistService get() {
    return new PlaylistService(executorService, playlistStorage(), FileUtils.getSupportedAudioFilesExtensions());
  }

  private PlaylistStorage playlistStorage() {
    var mapper = new XmlMapper();
    mapper.registerModules(new Jdk8Module());
    mapper.registerModules(new JavaTimeModule());
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return new XmlPlaylistStorage(mapper);
  }
}
