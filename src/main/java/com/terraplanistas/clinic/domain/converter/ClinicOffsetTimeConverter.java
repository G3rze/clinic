package com.terraplanistas.clinic.domain.converter;

import com.terraplanistas.clinic.config.ClinicTimezoneHolder;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

@Converter(autoApply = true)
public class ClinicOffsetTimeConverter implements AttributeConverter<OffsetTime, String> {

    @Override
    public String convertToDatabaseColumn(OffsetTime offsetTime) {
        if (offsetTime == null) {
            return null;
        }
        // Store as UTC (convert to UTC offset)
        OffsetTime utcTime = offsetTime.withOffsetSameInstant(ZoneOffset.UTC);
        return utcTime.toLocalTime().toString();
    }

    @Override
    public OffsetTime convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Read as UTC and convert to clinic timezone
        LocalTime localTime = LocalTime.parse(dbData);
        OffsetTime utcTime = localTime.atOffset(ZoneOffset.UTC);
        return utcTime.withOffsetSameInstant(ClinicTimezoneHolder.getClinicZone().getRules().getOffset(java.time.Instant.now()));
    }
}
