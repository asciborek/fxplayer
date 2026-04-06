package com.github.asciborek.util;

public interface StringEncryptor {

    byte[] encrypt(String plainText);

    String decrypt(byte[] cipherText);
}
