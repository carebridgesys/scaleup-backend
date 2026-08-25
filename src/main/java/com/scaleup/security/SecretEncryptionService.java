package com.scaleup.security;

public interface SecretEncryptionService {

    String encrypt(
            String plaintext
    );

    String decrypt(
            String ciphertext
    );
}