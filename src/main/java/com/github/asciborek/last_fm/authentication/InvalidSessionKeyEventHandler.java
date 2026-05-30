package com.github.asciborek.last_fm.authentication;

import com.github.asciborek.last_fm.InvalidSessionKeyEvent;
import com.github.asciborek.last_fm.LastFmUserService;
import com.github.asciborek.util.AutoRegistrableEventBusListener;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoRegistrableEventBusListener
public final class InvalidSessionKeyEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(InvalidSessionKeyEventHandler.class);

  private final LastFmUserService lastFmUserService;

  @Inject
  public InvalidSessionKeyEventHandler(LastFmUserService lastFmUserService) {
    this.lastFmUserService = lastFmUserService;
  }

  @Subscribe
  public void onInvalidSessionKeyEvent(InvalidSessionKeyEvent event) {
    LOG.info("received InvalidSessionKeyEvent event for user {}, removing user session data", event.username());
    lastFmUserService.deleteUserSession();
  }
}
