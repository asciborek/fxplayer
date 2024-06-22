package com.github.asciborek.last_fm;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class OpenLastFmSettingsCommandHandlerFactory implements Provider<OpenLastFmSettingsCommandHandler> {

  private final LastFmAuthenticationService lastFmAuthenticationService;
  private final EventBus eventBus;

  @Inject
  public OpenLastFmSettingsCommandHandlerFactory(
      LastFmAuthenticationService lastFmAuthenticationService,
      EventBus eventBus) {
    this.lastFmAuthenticationService = lastFmAuthenticationService;
    this.eventBus = eventBus;
  }

  @Override
  public OpenLastFmSettingsCommandHandler get() {
    var openLastFmSettingsCommandHandler = new OpenLastFmSettingsCommandHandler(
        lastFmAuthenticationService);
    eventBus.register(openLastFmSettingsCommandHandler);
    return openLastFmSettingsCommandHandler;
  }
}
