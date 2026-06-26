package com.terraplanistas.clinic.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class TimezoneConfig {

    @Value("${clinic.timezone}")
    private String clinicTimezone;

    @PostConstruct
    public void init() {
        ClinicTimezoneHolder.setClinicZone(ZoneId.of(clinicTimezone));
    }
}
