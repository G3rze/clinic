package com.terraplanistas.clinic.domain.entities;

import com.terraplanistas.clinic.domain.encryption.EncryptedStringConverter;
import com.terraplanistas.clinic.domain.enums.IdType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "patients", schema = "clinic")
@Getter
@Setter
public class Patient extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_name", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String lastName;

    @Column(name = "id_number", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String idNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false)
    private IdType idType;

    @Column(name = "address", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String address;

    @Column(name = "phones", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String phones;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "id_number_bindex")
    @Convert(converter = EncryptedStringConverter.class)
    private String idNumberBindex;

    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;

    @Column(name = "first_name_bindex")
    @Convert(converter = EncryptedStringConverter.class)
    private String firstNameBindex;

    @Column(name = "last_name_bindex")
    @Convert(converter = EncryptedStringConverter.class)
    private String lastNameBindex;
}