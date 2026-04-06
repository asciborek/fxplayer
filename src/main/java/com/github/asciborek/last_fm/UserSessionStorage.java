package com.github.asciborek.last_fm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.asciborek.util.StringEncryptor;
import java.nio.file.Path;
import java.util.Optional;

public class UserSessionStorage {

  private final ObjectMapper objectMapper;
  private final StringEncryptor stringEncryptor;
  private final Path sessionFilePath;

  public UserSessionStorage(ObjectMapper objectMapper, StringEncryptor stringEncryptor, Path sessionFilePath) {
    this.objectMapper = objectMapper;
    this.stringEncryptor = stringEncryptor;
    this.sessionFilePath = sessionFilePath;
  }

  void save(UserSession userSession) {
    byte[] ciphered = stringEncryptor.encrypt(userSession.token());
    var data = new UserSessionWithEncryptedSessionToken(userSession.username(), ciphered);
    try {
      objectMapper.writeValue(sessionFilePath.toFile(), data);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  Optional<UserSession> load() {
    if (!sessionFilePath.toFile().exists()) {
      return Optional.empty();
    }
    try {
      var data = objectMapper.readValue(sessionFilePath.toFile(), UserSessionWithEncryptedSessionToken.class);
      String token = stringEncryptor.decrypt(data.cipheredToken());
      return Optional.of(new UserSession(data.username(), token));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  record UserSessionWithEncryptedSessionToken(String username, byte[] cipheredToken) {}
}
