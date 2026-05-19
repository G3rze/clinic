package com.terraplanistas.clinic.domain.entities;

import com.terraplanistas.clinic.domain.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;

@Entity
@Table(name = "medical_records", schema = "clinic")
@Getter
@Setter
public class MedicalRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "diagnosis_code", nullable = false)
    private String diagnosisCode;

    @Column(name = "diagnosis_description", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String diagnosisDescription;

    @Column(name = "clinical_notes", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String clinicalNotes;

    @Column(name = "physical_examination")
    @Convert(converter = EncryptedStringConverter.class)
    private String physicalExamination;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments")
    private List<String> attachments = List.of();
}