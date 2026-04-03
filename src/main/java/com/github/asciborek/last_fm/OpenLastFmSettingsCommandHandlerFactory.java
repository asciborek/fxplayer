package com.github.asciborek.last_fm;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

final class OpenLastFmSettingsCommandHandlerFactory implements Provider<OpenLastFmSettingsCommandHandler> {

  private final EventBus eventBus;
  private final LastFmAuthenticationHandler lastFmAuthenticationHandler;
  private final LastFmUserService lastFmUserService;

  @Inject
  public OpenLastFmSettingsCommandHandlerFactory(EventBus eventBus,
      LastFmAuthenticationHandler lastFmAuthenticationHandler,
      LastFmUserService lastFmUserService) {
    this.eventBus = eventBus;
    this.lastFmAuthenticationHandler = lastFmAuthenticationHandler;
    this.lastFmUserService = lastFmUserService;
  }

  @Override
  public OpenLastFmSettingsCommandHandler get() {
    var openLastFmSettingsCommandHandler = new OpenLastFmSettingsCommandHandler(lastFmAuthenticationHandler, lastFmUserService, eventBus);
    eventBus.register(openLastFmSettingsCommandHandler);
    return openLastFmSettingsCommandHandler;
  }

}
