package com.github.asciborek.util;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadFactory;

public class AEADStringEncryptor implements StringEncryptor {

  private final Aead aead;

  public AEADStringEncryptor(KeysetHandle keysetHandle) throws Exception {
    this.aead = AeadFactory.getPrimitive(keysetHandle);
  }

  @Override
   public byte[] encrypt(String plainText) {
    try {
      return aead.encrypt(plainText.getBytes(), null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
   }

   @Override
   public String decrypt(byte[] cipherText) {
    try {
      return new String(aead.decrypt(cipherText, null));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
   }

}
