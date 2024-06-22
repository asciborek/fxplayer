package com.github.asciborek.last_fm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;

public class LastFmAuthenticationHandlerFactory implements Provider<LastFmAuthenticationService> {

  private final HttpClient httpClient;
  private final ExecutorService executorService;
  private final ObjectMapper objectMapper;
  private final String apiKey;

  @Inject
  public LastFmAuthenticationHandlerFactory(HttpClient httpClient,
      ExecutorService executorService, ObjectMapper objectMapper, @Named("lastFmApiKey") String apiKey) {
    this.httpClient = httpClient;
    this.executorService = executorService;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
  }

  @Override
  public LastFmAuthenticationService get() {
    return new LastFmAuthenticationService(httpClient, executorService, objectMapper, apiKey);
  }
}
