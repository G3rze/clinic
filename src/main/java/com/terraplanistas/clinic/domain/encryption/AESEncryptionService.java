package com.terraplanistas.clinic.domain.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AESEncryptionService implements EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_LENGTH = 32;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public AESEncryptionService(
            @Value("${encryption.key}") String encodedKey) {
        this.secretKey = loadKey(encodedKey);
        this.secureRandom = new SecureRandom();
    }

    private SecretKey loadKey(String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException(
                "Encryption key not found in encryption.key property. " +
                "Please set a 256-bit Base64-encoded AES key.");
        }
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        if (decodedKey.length != KEY_LENGTH) {
            throw new IllegalStateException(
                "Invalid encryption key length. Expected 32 bytes (256 bits), got " + decodedKey.length);
        }
        return new SecretKeySpec(decodedKey, "AES");
    }

    @Override
    public byte[] encrypt(String plaintext) throws EncryptionException {
        if (plaintext == null || plaintext.isEmpty()) {
            return new byte[0];
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return combined;
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(byte[] ciphertext) throws EncryptionException {
        if (ciphertext == null || ciphertext.length == 0) {
            return "";
        }
        if (ciphertext.length < GCM_IV_LENGTH) {
            throw new EncryptionException("Invalid ciphertext: too short");
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[ciphertext.length - GCM_IV_LENGTH];
            System.arraycopy(ciphertext, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plaintext = cipher.doFinal(encrypted);
            return new String(plaintext);
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }
}