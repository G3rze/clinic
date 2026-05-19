package com.terraplanistas.clinic.domain.encryption;

public interface EncryptionService {
    byte[] encrypt(String plaintext) throws EncryptionException;
    String decrypt(byte[] ciphertext) throws EncryptionException;
}