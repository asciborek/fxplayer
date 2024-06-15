package com.github.asciborek.album_cover;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("UnstableApiUsage")//Guava
public final class AlbumCoverControllerFactory implements Provider<AlbumCoverController> {

  private final EventBus eventBus;
  private final HttpClient httpClient;
  private final ExecutorService executorService;
  private final ObjectMapper objectMapper;
  private final String lastFmApiKey;

  @Inject
  public AlbumCoverControllerFactory(EventBus eventBus, HttpClient httpClient,
      ExecutorService executorService, ObjectMapper objectMapper, @Named("lastFmApiKey") String lastFmApiKey) {
    this.eventBus = eventBus;
    this.httpClient = httpClient;
    this.executorService = executorService;
    this.objectMapper = objectMapper;
    this.lastFmApiKey = lastFmApiKey;
  }

  @Override
  public AlbumCoverController get() {
    var delegateProvider = new LastFmAlbumCoverProvider(httpClient, executorService, objectMapper, lastFmApiKey);
    var controller = new AlbumCoverController(new CachingAlbumCoverProvider(delegateProvider));
    eventBus.register(controller);
    return controller;
  }
}
