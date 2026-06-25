package com.terraplanistas.clinic.domain.entities;

import com.terraplanistas.clinic.domain.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users", schema = "clinic")
@Getter
@Setter
public class User extends BaseUserEntity {

    @Column(name = "google_user_id", unique = true)
    private String googleUserId;

    @Column(name = "email", unique = true)
    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Column(name = "username")
    @Convert(converter = EncryptedStringConverter.class)
    private String username;

    @Column(name = "email_bindex")
    private String emailBindex;

    @Column(name = "username_bindex")
    private String usernameBindex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_access_revoked", nullable = false)
    private boolean isAccessRevoked = false;

    @Column(name = "anonymization_permanent_at")
    private OffsetDateTime anonymizationPermanentAt;
}