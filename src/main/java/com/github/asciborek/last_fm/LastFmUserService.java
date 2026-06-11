package com.github.asciborek.last_fm;

import com.github.asciborek.last_fm.authentication.UserAuthenticationEvent;
import com.github.asciborek.settings.LastFmSettings;
import com.github.asciborek.settings.SettingsService;
import com.google.common.eventbus.EventBus;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LastFmUserService {

  private static final Logger LOG = LoggerFactory.getLogger(LastFmUserService.class);

  private final SettingsService settingsService;
  private final UserSessionStorage userSessionStorage;
  private final EventBus eventBus;

  private volatile UserSession userSession = null;

  public LastFmUserService(SettingsService settingsService, UserSessionStorage userSessionStorage, EventBus eventBus) {
    this.settingsService = settingsService;
    this.userSessionStorage = userSessionStorage;
    this.eventBus = eventBus;
    userSessionStorage.load().ifPresent(storedUserSession -> this.userSession = storedUserSession);
  }

  public Optional<UserSession> getUserSession() {
    return Optional.ofNullable(userSession);
  }

  public LastFmSettings getLastFmSettings() {
    return settingsService.getLastFmSettings();
  }

  public void setLastFmSettings(LastFmSettings lastFmSettings) {
    this.settingsService.setLastFmSettings(lastFmSettings);
  }

  public boolean isOnlineScrobblingEnabled() {
    LastFmSettings settings = getLastFmSettings();
    return settings.scrobblingEnabled() && (!settings.offlineModeEnabled());
  }

  public boolean isOnlineScrobblingDisabled() {
    return !isOnlineScrobblingEnabled();
  }

  public void updateUserSession(UserSession userSession) {
    LOG.info("Updated User Session for {}", userSession.username());
    userSessionStorage.save(userSession);
    this.userSession = userSession;
    eventBus.post(new UserAuthenticationEvent.UserAuthenticatedEvent(userSession));
  }

  public boolean deleteUserSession() {
    boolean deleted = userSessionStorage.delete();
    if (deleted) {
      userSession = null;
    }
    return deleted;
  }

}
