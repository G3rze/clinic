package com.terraplanistas.clinic.domain.entities;

import com.terraplanistas.clinic.domain.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "prescriptions", schema = "clinic")
@Getter
@Setter
public class Prescription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "medicine_snapshot", nullable = false)
    private Map<String, Object> medicineSnapshot;

    @Column(name = "dosage_instructions", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String dosageInstructions;

    @Column(name = "digital_signature", nullable = false)
    private String digitalSignature;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "max_usages", nullable = false)
    private Integer maxUsages = 3;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private java.util.UUID createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = java.time.OffsetDateTime.now();
        }
    }
}