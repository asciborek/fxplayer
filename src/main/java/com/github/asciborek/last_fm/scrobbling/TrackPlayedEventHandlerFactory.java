package com.github.asciborek.last_fm.scrobbling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.asciborek.last_fm.LastFmUserService;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;

public class TrackPlayedEventHandlerFactory implements Provider<TrackEventsHandler> {

  private final EventBus eventBus;
  private final HttpClient httpClient;
  private final ExecutorService executorService;
  private final ObjectMapper objectMapper;
  private final LastFmUserService lastFmUserService;
  private final String apiKey;
  private final String sharedSecret;

  @Inject
  public TrackPlayedEventHandlerFactory(EventBus eventBus, HttpClient httpClient, ExecutorService executorService,
      ObjectMapper objectMapper, LastFmUserService lastFmUserService, @Named("lastFmApiKey") String apiKey, @Named("lastFmSharedSecret") String sharedSecret) {
    this.eventBus = eventBus;
    this.httpClient = httpClient;
    this.executorService = executorService;
    this.objectMapper = objectMapper;
    this.lastFmUserService = lastFmUserService;
    this.apiKey = apiKey;
    this.sharedSecret = sharedSecret;
  }

  @Override
  public TrackEventsHandler get() {
    TrackApiClient trackApiClient = new TrackApiClient(httpClient, objectMapper, apiKey, sharedSecret);
    var trackPlayedEventHandler = new TrackEventsHandler(lastFmUserService, trackApiClient, executorService);
    eventBus.register(trackPlayedEventHandler);
    return trackPlayedEventHandler;
  }
}
