package com.github.asciborek.last_fm;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class OpenLastFmSettingsCommandHandlerFactory implements Provider<OpenLastFmSettingsCommandHandler> {

  private final LastFmAuthenticationHandler lastFmAuthenticationHandler;
  private final EventBus eventBus;

  @Inject
  public OpenLastFmSettingsCommandHandlerFactory(LastFmAuthenticationHandler lastFmAuthenticationHandler,
      EventBus eventBus) {
    this.lastFmAuthenticationHandler = lastFmAuthenticationHandler;
    this.eventBus = eventBus;
  }

  @Override
  public OpenLastFmSettingsCommandHandler get() {
    var openLastFmSettingsCommandHandler = new OpenLastFmSettingsCommandHandler(lastFmAuthenticationHandler);
    eventBus.register(openLastFmSettingsCommandHandler);
    return openLastFmSettingsCommandHandler;
  }
}
