package com.terraplanistas.clinic.domain.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, byte[]> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService service) {
        encryptionService = service;
    }

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return new byte[0];
        }
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length == 0) {
            return "";
        }
        return encryptionService.decrypt(dbData);
    }
}