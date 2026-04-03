package com.github.asciborek.last_fm;

import com.google.common.eventbus.EventBus;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LastFmUserService {

  private static final Logger LOG = LoggerFactory.getLogger(LastFmUserService.class);

  private final EventBus eventBus;

  public LastFmUserService(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  private AtomicReference<UserSession> userSession = new AtomicReference<>();

  public Optional<UserSession> getUserSession() {
    return Optional.ofNullable(userSession.get());
  }

  public void updateUserSession(UserSession userSession) {
    LOG.info("Updated User Session for {}", userSession.username());
    this.userSession.set(userSession);
    eventBus.post(new UserAuthenticationEvent.UserAuthenticatedEvent(userSession));

  }
}
