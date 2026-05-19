package com.terraplanistas.clinic.domain.entities;

import com.terraplanistas.clinic.domain.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users", schema = "clinic")
@Getter
@Setter
public class User extends BaseUserEntity {

    @Column(name = "email", unique = true)
    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Column(name = "username")
    @Convert(converter = EncryptedStringConverter.class)
    private String username;

    @Column(name = "email_bindex")
    @Convert(converter = EncryptedStringConverter.class)
    private String emailBindex;

    @Column(name = "username_bindex")
    @Convert(converter = EncryptedStringConverter.class)
    private String usernameBindex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}