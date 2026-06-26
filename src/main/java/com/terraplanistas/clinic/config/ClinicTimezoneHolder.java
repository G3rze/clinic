package com.terraplanistas.clinic.config;

import java.time.ZoneId;

public class ClinicTimezoneHolder {

    private static ZoneId clinicZone = ZoneId.of("America/El_Salvador");

    public static ZoneId getClinicZone() {
        return clinicZone;
    }

    public static void setClinicZone(ZoneId zone) {
        clinicZone = zone;
    }
}
