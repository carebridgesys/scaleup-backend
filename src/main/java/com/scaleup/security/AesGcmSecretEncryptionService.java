package com.scaleup.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesGcmSecretEncryptionService
        implements SecretEncryptionService {

    private static final String TRANSFORMATION =
            "AES/GCM/NoPadding";

    private static final int IV_LENGTH =
            12;

    private static final int TAG_LENGTH_BITS =
            128;

    private final SecretKeySpec secretKey;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public AesGcmSecretEncryptionService(
            @Value("${security.credential-encryption-key}")
            String encodedKey
    ) {

        if (
                encodedKey == null
                        || encodedKey.isBlank()
        ) {

            throw new IllegalStateException(
                    "Credential encryption key is not configured."
            );
        }

        byte[] keyBytes =
                Base64.getDecoder()
                        .decode(
                                encodedKey.trim()
                        );

        if (keyBytes.length != 32) {

            throw new IllegalStateException(
                    "Credential encryption key must be a 256-bit Base64 encoded key."
            );
        }

        this.secretKey =
                new SecretKeySpec(
                        keyBytes,
                        "AES"
                );
    }

    @Override
    public String encrypt(
            String plaintext
    ) {

        if (
                plaintext == null
                        || plaintext.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Secret must not be blank."
            );
        }

        try {

            byte[] iv =
                    new byte[IV_LENGTH];

            secureRandom.nextBytes(
                    iv
            );

            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            TAG_LENGTH_BITS,
                            iv
                    )
            );

            byte[] encrypted =
                    cipher.doFinal(
                            plaintext
                                    .trim()
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );

            return "v1:"
                    + Base64.getEncoder()
                    .encodeToString(iv)
                    + ":"
                    + Base64.getEncoder()
                    .encodeToString(encrypted);

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to encrypt credential.",
                    exception
            );
        }
    }

    @Override
    public String decrypt(
            String ciphertext
    ) {

        if (
                ciphertext == null
                        || ciphertext.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Encrypted credential must not be blank."
            );
        }

        try {

            String[] parts =
                    ciphertext.split(
                            ":",
                            3
                    );

            if (
                    parts.length != 3
                            || !"v1".equals(parts[0])
            ) {

                throw new IllegalArgumentException(
                        "Unsupported encrypted credential format."
                );
            }

            byte[] iv =
                    Base64.getDecoder()
                            .decode(
                                    parts[1]
                            );

            byte[] encrypted =
                    Base64.getDecoder()
                            .decode(
                                    parts[2]
                            );

            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            TAG_LENGTH_BITS,
                            iv
                    )
            );

            byte[] decrypted =
                    cipher.doFinal(
                            encrypted
                    );

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to decrypt credential.",
                    exception
            );
        }
    }
}