package com.terraplanistas.clinic.domain.entities;

import com.terraplanistas.clinic.domain.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post_appointment_notes", schema = "clinic")
@Getter
@Setter
public class PostAppointmentNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "content", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String content;
}