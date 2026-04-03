package com.github.asciborek.last_fm;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class LastFmSessionServiceFactory implements Provider<LastFmUserService> {

  private final EventBus eventBus;

  @Inject
  public LastFmSessionServiceFactory(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public LastFmUserService get() {
    return new LastFmUserService(eventBus);
  }

}
