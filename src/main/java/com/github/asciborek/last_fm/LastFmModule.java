package com.github.asciborek.last_fm;

import com.github.asciborek.last_fm.authentication.LastFmAuthenticationHandler;
import com.github.asciborek.last_fm.authentication.LastFmAuthenticationHandlerFactory;
import com.github.asciborek.last_fm.scrobbling.ScrobblesDao;
import com.github.asciborek.last_fm.scrobbling.ScrobblesOutboxProcessor;
import com.github.asciborek.last_fm.scrobbling.ScrobblesOutboxProcessorFactory;
import com.github.asciborek.last_fm.scrobbling.StartPlayingTrackEventHandler;
import com.github.asciborek.last_fm.scrobbling.TrackApiService;
import com.github.asciborek.last_fm.scrobbling.TrackPlayedEventHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class LastFmModule extends AbstractModule {

  private final Properties lastFmProperties;

  public LastFmModule(Properties lastFmProperties) {
    this.lastFmProperties = lastFmProperties;
  }

  private static final String API_KEY_PROPERTY_NAME = "api_key";
  private static final String SHARED_SECRET_PROPERTY_NAME = "shared_secret";

  @Override
  protected void configure() {
    //bind config and http client
    bind(HttpClient.class).toProvider(this::lastFmHttpClient).in(Scopes.SINGLETON);
    bind(String.class).annotatedWith(Names.named("lastFmApiKey")).toInstance(lastFmProperties.getProperty(API_KEY_PROPERTY_NAME));
    bind(String.class).annotatedWith(Names.named("lastFmSharedSecret")).toInstance(lastFmProperties.getProperty(SHARED_SECRET_PROPERTY_NAME));

    //bind user authentication components
    bind(LastFmUserService.class).toProvider(LastFmUserServiceFactory.class).in(Scopes.SINGLETON);
    bind(LastFmAuthenticationHandler.class).toProvider(LastFmAuthenticationHandlerFactory.class).in(Scopes.SINGLETON);
    bind(OpenLastFmSettingsCommandHandler.class).toProvider(OpenLastFmSettingsCommandHandlerFactory.class).asEagerSingleton();

    //bind track-related components
    bind(ScheduledExecutorService.class).toProvider(this::scheduledExecutorService).in(Scopes.SINGLETON); //outbox worker executor service
    bind(TrackApiService.class).in(Scopes.SINGLETON);
    bind(ScrobblesDao.class).in(Scopes.SINGLETON);
    bind(StartPlayingTrackEventHandler.class).asEagerSingleton();
    bind(TrackPlayedEventHandler.class).asEagerSingleton();
    bind(ScrobblesOutboxProcessor.class).toProvider(ScrobblesOutboxProcessorFactory.class).asEagerSingleton();
  }

  public HttpClient lastFmHttpClient() {
    return HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }

  public ScheduledExecutorService scheduledExecutorService() {
    return Executors.newSingleThreadScheduledExecutor();
  }

}
