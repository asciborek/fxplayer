package com.github.asciborek.last_fm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;

public class LastFmAuthenticationHandlerFactory implements Provider<LastFmAuthenticationHandler> {

  private final ExecutorService executorService;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final XmlMapper xmlMapper;
  private final EventBus eventBus;
  private final String apiKey;
  private final String sharedSecret;

  @Inject
  public LastFmAuthenticationHandlerFactory(ExecutorService executorService, HttpClient httpClient,
      ObjectMapper objectMapper, XmlMapper xmlMapper, EventBus eventBus,
      @Named("lastFmApiKey") String apiKey, @Named("lastFmSharedSecret") String sharedSecret) {
    this.executorService = executorService;
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.xmlMapper = xmlMapper;
    this.eventBus = eventBus;
    this.apiKey = apiKey;
    this.sharedSecret = sharedSecret;
  }

  @Override
  public LastFmAuthenticationHandler get() {
    LastFmAuthenticationHandler handler = new LastFmAuthenticationHandler(executorService, httpClient, objectMapper, apiKey, sharedSecret);
    eventBus.register(handler);
    return handler;
  }

}
