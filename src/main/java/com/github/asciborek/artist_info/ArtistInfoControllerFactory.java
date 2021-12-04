package com.github.asciborek.artist_info;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("UnstableApiUsage")//Guava
public final class ArtistInfoControllerFactory implements Provider<ArtistInfoController> {

  private final EventBus eventBus;
  private final HttpClient httpClient;
  private final ExecutorService executorService;
  private final String lastFmApiKey;

  @Inject
  public ArtistInfoControllerFactory(EventBus eventBus, HttpClient httpClient,
      ExecutorService executorService, String lastFmApiKey) {
    this.eventBus = eventBus;
    this.httpClient = httpClient;
    this.executorService = executorService;
    this.lastFmApiKey = lastFmApiKey;
  }

  @Override
  public ArtistInfoController get() {
    var lastFmArtistInfoProvider = new LastFmArtistInfoProvider(httpClient, executorService, lastFmApiKey);
    var controller = new ArtistInfoController(new CachingArtistInfoProvider(lastFmArtistInfoProvider));
    eventBus.register(controller);
    return controller;
  }
}
