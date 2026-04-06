package com.github.asciborek.util;

import static com.github.asciborek.util.FileUtils.getApplicationDataDirectory;

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.inject.Provider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class StringEncryptorFactory implements Provider<StringEncryptor> {

    private static final Path KEY_FILE_PATH = getApplicationDataDirectory().resolve("keyset.json");

    @Override
    public StringEncryptor get() {
      try {
        AeadConfig.register();
      } catch (GeneralSecurityException e) {
        throw new RuntimeException(e);
      }
      KeysetHandle keysetHandle = loadFromFile().orElseGet(this::createNewKeyset);
      try {
        return new AEADStringEncryptor(keysetHandle);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private Optional<KeysetHandle> loadFromFile() {
      if (Files.exists(KEY_FILE_PATH)) {
        try {
          KeysetHandle keysetHandle = CleartextKeysetHandle.read(
              JsonKeysetReader.withFile(KEY_FILE_PATH.toFile())
          );
          return Optional.of(keysetHandle);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      return Optional.empty();
    }

    private KeysetHandle createNewKeyset() {
      try {
        KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);
        CleartextKeysetHandle.write(
            keysetHandle, JsonKeysetWriter.withFile(KEY_FILE_PATH.toFile())
        );
        return keysetHandle;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

}
