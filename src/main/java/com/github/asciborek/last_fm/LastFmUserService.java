package com.github.asciborek.last_fm;

import com.google.common.eventbus.EventBus;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LastFmUserService {

  private static final Logger LOG = LoggerFactory.getLogger(LastFmUserService.class);

  private final UserSessionStorage userSessionStorage;
  private final EventBus eventBus;

  private final AtomicReference<UserSession> userSession = new AtomicReference<>();

  public LastFmUserService(UserSessionStorage userSessionStorage, EventBus eventBus) {
    this.userSessionStorage = userSessionStorage;
    this.eventBus = eventBus;
    userSessionStorage.load().ifPresent(userSession::set);
  }

  public Optional<UserSession> getUserSession() {
    return Optional.ofNullable(userSession.get());
  }

  public void updateUserSession(UserSession userSession) {
    LOG.info("Updated User Session for {}", userSession.username());
    userSessionStorage.save(userSession);
    this.userSession.set(userSession);
    eventBus.post(new UserAuthenticationEvent.UserAuthenticatedEvent(userSession));

  }
}
