package com.terraplanistas.clinic.domain.entities;

import com.terraplanistas.clinic.domain.encryption.EncryptedStringConverter;
import com.terraplanistas.clinic.domain.enums.IdType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employees", schema = "clinic")
@Getter
@Setter
public class Employee extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
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
}