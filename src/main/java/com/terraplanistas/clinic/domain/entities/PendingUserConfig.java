package com.terraplanistas.clinic.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_user_config", schema = "clinic")
@Getter
@Setter
public class PendingUserConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "google_user_id", nullable = false)
    private String googleUserId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven = false;

    @Column(name = "profile_complete", nullable = false)
    private boolean profileComplete = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public boolean isExpired(int ttlMinutes) {
        return createdAt.plusMinutes(ttlMinutes).isBefore(OffsetDateTime.now());
    }
}
