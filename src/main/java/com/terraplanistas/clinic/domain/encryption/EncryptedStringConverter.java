package com.terraplanistas.clinic.domain.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, byte[]> {

    private static final int GCM_IV_LENGTH = 12;

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return new byte[0];
        }
        return SpringContext.getBean(EncryptionService.class).encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length == 0) {
            return "";
        }
        if (dbData.length < GCM_IV_LENGTH) {
            return "";
        }
        return SpringContext.getBean(EncryptionService.class).decrypt(dbData);
    }
}