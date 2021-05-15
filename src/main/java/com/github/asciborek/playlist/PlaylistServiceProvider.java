package com.github.asciborek.playlist;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.List;
import java.util.concurrent.ExecutorService;

public final class PlaylistServiceProvider implements Provider<PlaylistService> {

  private static final List<String> SUPPORTED_AUDIO_FILES_EXTENSIONS = List.of(".mp3");
  private final ExecutorService executorService;

  @Inject
  public PlaylistServiceProvider(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public PlaylistService get() {
    return new PlaylistService(executorService, SUPPORTED_AUDIO_FILES_EXTENSIONS);
  }
}
