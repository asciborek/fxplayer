package com.github.asciborek.last_fm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LastFmAuthenticationHandler {

  private static final Logger LOG = LoggerFactory.getLogger(LastFmAuthenticationHandler.class);
  private static final int HTTP_OK = 200;
  private static final List<String> LINUX_BROWSER_COMMANDS = List.of(
      "xdg-open",
      "gio",
      "gnome-open",
      "kde-open",
      "sensible-browser");
  
  private static final String AUTH_REQUEST_TOKEN_URI_TEMPLATE =
      "https://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=%s&format=json";

  private static final String USER_AUTH_URI_TEMPLATE =
    "https://www.last.fm/api/auth/?api_key=%s&token=%s";


  private final ExecutorService executorService;
  private final Executor delayedExecutor;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final SessionTokenFetcher sessionTokenFetcher;
  private final LastFmUserService lastFmUserService;
  private final String apiKey;


  LastFmAuthenticationHandler(ExecutorService executorService,
      HttpClient httpClient, ObjectMapper objectMapper, SessionTokenFetcher sessionTokenFetcher,
      LastFmUserService lastFmUserService, String apiKey) {
    this.executorService = executorService;
    this.delayedExecutor = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS, executorService);
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
    this.sessionTokenFetcher = sessionTokenFetcher;
    this.lastFmUserService = lastFmUserService;
  }

  void authenticate() {
    LOG.info("last.fm authentication requested");
    CompletableFuture.supplyAsync(this::fetchRequestToken, executorService)
        .thenApply(this::openUserAuthorization)
        .thenApplyAsync(sessionTokenFetcher::fetchSessionToken, delayedExecutor)
        .thenAccept(response -> {
          if (response instanceof LastFmSessionResponse.LastFmSessionSuccessResponse(
              LastFmSessionResponse.Session session
          )) {
            LOG.info("Successfully authenticated with last.fm, username {}", session.name());
            lastFmUserService.updateUserSession(new UserSession(session.name(), session.key()));
          }
        });
  }

  private LastFmRequestToken fetchRequestToken() {
    var requestUri = String.format(AUTH_REQUEST_TOKEN_URI_TEMPLATE, apiKey);
    var request = HttpRequest.newBuilder()
        .uri(URI.create(requestUri))
        .GET()
        .build();

    try {
      var response = httpClient.send(request, BodyHandlers.ofString());
      if (response.statusCode() != HTTP_OK) {
        throw new LastFmAuthenticationException(response.statusCode());
      }
      return objectMapper.readValue(response.body(), LastFmRequestToken.class);
    } catch (IOException e) {
      throw new LastFmAuthenticationException("Error while fetching last.fm request token");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new LastFmAuthenticationException("Interrupted while fetching last.fm request token");
    }
  }

  private LastFmRequestToken openUserAuthorization(LastFmRequestToken token) {
    LOG.info("Received request token, opening user authorization page");
    var authUri = URI.create(String.format(
        USER_AUTH_URI_TEMPLATE,
        URLEncoder.encode(apiKey, StandardCharsets.UTF_8),
        URLEncoder.encode(token.token(), StandardCharsets.UTF_8)));

    try {
      if (Desktop.isDesktopSupported()
          && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(authUri);
        LOG.info("Opened authorization page: {}", authUri);
      } else {
        openBrowserWithLinuxFallback(authUri);
      }
    } catch (IOException e) {
      openBrowserWithLinuxFallback(authUri);
    }
    return token;
  }

  private void openBrowserWithLinuxFallback(URI authUri) {
    if (!isLinux()) {
      LOG.warn("Desktop browse not supported. Open this URL manually: {}", authUri);
      return;
    }

    for (var command : LINUX_BROWSER_COMMANDS) {
      if (tryOpenWithCommand(command, authUri)) {
        LOG.info("Opened authorization page with {}: {}", command, authUri);
        return;
      }
    }

    throw new LastFmAuthenticationException(
        "Could not open browser for last.fm auth. Open this URL manually: " + authUri);
  }

  private boolean tryOpenWithCommand(String command, URI authUri) {
    try {
      var processBuilder = command.equals("gio")
          ? new ProcessBuilder(command, "open", authUri.toString())
          : new ProcessBuilder(command, authUri.toString());
      processBuilder.start();
      return true;
    } catch (IOException ignored) {
      return false;
    }
  }

  private boolean isLinux() {
    return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
  }

}
