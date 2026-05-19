package com.terraplanistas.clinic.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "patient_representatives", schema = "clinic")
@Getter
@Setter
public class PatientRepresentative extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_user_id", nullable = false)
    private User representativeUser;

    @Column(name = "relationship_type", nullable = false)
    private String relationshipType;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}