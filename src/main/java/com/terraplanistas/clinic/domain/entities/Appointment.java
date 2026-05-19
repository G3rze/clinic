package com.terraplanistas.clinic.domain.entities;

import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.domain.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "appointments", schema = "clinic")
@Getter
@Setter
public class Appointment extends BaseEntity {

    @Column(name = "google_envent_id", nullable = false, unique = true)
    private String googleEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Column(name = "final_fee_per_hour", nullable = false)
    private BigDecimal finalFeePerHour = BigDecimal.ZERO;

    @Column(name = "score")
    private Integer score = 0;

    @Column(name = "review")
    @Convert(converter = EncryptedStringConverter.class)
    private String review;

    @Column(name = "registered_at", nullable = false)
    private OffsetDateTime registeredAt;

    @Column(name = "expected_at", nullable = false)
    private OffsetDateTime expectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_caller_user_id", nullable = false)
    private User patientCallerUser;
}