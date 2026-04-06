package com.github.asciborek.last_fm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.asciborek.util.FileUtils;
import com.github.asciborek.util.StringEncryptor;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class LastFmSessionServiceFactory implements Provider<LastFmUserService> {

  private final EventBus eventBus;
  private final StringEncryptor stringEncryptor;
  private final ObjectMapper objectMapper;

  @Inject
  public LastFmSessionServiceFactory(EventBus eventBus, StringEncryptor stringEncryptor, ObjectMapper objectMapper) {
    this.eventBus = eventBus;
    this.stringEncryptor = stringEncryptor;
    this.objectMapper = objectMapper;
  }

  @Override
  public LastFmUserService get() {
    var sessionFilePath = FileUtils.getApplicationDataDirectory().resolve("last_fm_session.json");
    var userSessionStorage = new UserSessionStorage(objectMapper, stringEncryptor, sessionFilePath);
    return new LastFmUserService(userSessionStorage, eventBus);
  }

}
