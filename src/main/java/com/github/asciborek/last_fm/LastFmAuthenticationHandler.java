package com.github.asciborek.last_fm;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LastFmAuthenticationHandler {

  private static final Logger LOG = LoggerFactory.getLogger(LastFmAuthenticationHandler.class);

  private final HttpClient httpClient;
  private final ExecutorService executorService;
  private final ObjectMapper objectMapper;
  private final String apiKey;

  public LastFmAuthenticationHandler(HttpClient httpClient,
      ExecutorService executorService,
      ObjectMapper objectMapper,
      String apiKey) {
    this.httpClient = httpClient;
    this.executorService = executorService;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
  }

  public void signInToLastFm() {
    CompletableFuture.supplyAsync(new GetTokenTask(httpClient, apiKey, objectMapper), executorService)
        .thenAccept(token -> LOG.info("token fetched :{}", token));
  }
}
