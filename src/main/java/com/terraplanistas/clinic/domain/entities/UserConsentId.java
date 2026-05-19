package com.terraplanistas.clinic.domain.entities;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class UserConsentId implements Serializable {
    private UUID userId;
    private String version;
    private UUID patientId;

    public UserConsentId() {}

    public UserConsentId(UUID userId, String version, UUID patientId) {
        this.userId = userId;
        this.version = version;
        this.patientId = patientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserConsentId that = (UserConsentId) o;
        return userId != null && userId.equals(that.userId)
            && version != null && version.equals(that.version)
            && patientId != null && patientId.equals(that.patientId);
    }

    @Override
    public int hashCode() {
        return 31 + (userId != null ? userId.hashCode() : 0)
            + (version != null ? version.hashCode() : 0)
            + (patientId != null ? patientId.hashCode() : 0);
    }
}