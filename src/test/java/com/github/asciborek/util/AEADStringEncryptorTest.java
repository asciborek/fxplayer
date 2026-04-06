package com.github.asciborek.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AEADStringEncryptorTest {

  private KeysetHandle keysetHandle;

  @BeforeAll
  void beforeAll() throws Exception {
    AeadConfig.register();
    keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);
  }

  static Stream<Arguments> encryptAndDecryptTestData() {
    return Stream.of(
        Arguments.of("simple string"),
        Arguments.of("!@#$%^&*()_+"),
        Arguments.of("你好世界"),
        Arguments.of(""),
        Arguments.of("string with spaces and tabs \t  \t")
    );
  }

  @ParameterizedTest
  @MethodSource("encryptAndDecryptTestData")
  void encryptAndDecryptTest(String plain) throws Exception {
    AEADStringEncryptor encryptor = new AEADStringEncryptor(keysetHandle);
    byte[] cipherText = encryptor.encrypt(plain);
    String decrypted = encryptor.decrypt(cipherText);
    Assertions.assertThat(decrypted).isEqualTo(plain);
  }

}