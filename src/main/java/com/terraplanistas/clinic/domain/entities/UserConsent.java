package com.terraplanistas.clinic.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_consents", schema = "clinic")
@IdClass(UserConsentId.class)
@Getter
@Setter
public class UserConsent {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "version")
    private String version;

    @Id
    @Column(name = "patient_id")
    private UUID patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;

    @Column(name = "treatment_purpose", nullable = false)
    private Boolean treatmentPurpose = false;

    @Column(name = "data_analysis_purpose", nullable = false)
    private Boolean dataAnalysisPurpose = false;

    @Column(name = "consented_at", nullable = false)
    private OffsetDateTime consentedAt;

    @PrePersist
    protected void onCreate() {
        if (consentedAt == null) {
            consentedAt = OffsetDateTime.now();
        }
    }
}