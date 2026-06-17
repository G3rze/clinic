package com.terraplanistas.clinic.http.session;

import com.terraplanistas.clinic.domain.entities.BaseEntity;
import com.terraplanistas.clinic.domain.encryption.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "google_sessions", schema = "clinic")
@Getter
@Setter
public class GoogleSession extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "google_user_id", nullable = false, unique = true)
    private String googleUserId;

    @Column(name = "access_token", nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String accessToken;

    @Column(name = "refresh_token")
    @Convert(converter = EncryptedStringConverter.class)
    private String refreshToken;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "google_email")
    private String googleEmail;

    @Column(name = "jwt_access_token")
    @Convert(converter = EncryptedStringConverter.class)
    private String jwtAccessToken;

    @Column(name = "jwt_refresh_token")
    @Convert(converter = EncryptedStringConverter.class)
    private String jwtRefreshToken;

    @Column(name = "jwt_expires_at")
    private OffsetDateTime jwtExpiresAt;

    @Column(name = "refresh_token_updated_at")
    private OffsetDateTime refreshTokenUpdatedAt;

    public boolean isExpired() {
        return expiresAt != null && OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isEmpty();
    }

    public boolean isJwtExpired() {
        return jwtExpiresAt != null && OffsetDateTime.now().isAfter(jwtExpiresAt);
    }

    public boolean hasJwtRefreshToken() {
        return jwtRefreshToken != null && !jwtRefreshToken.isEmpty();
    }

    public void clearJwtTokens() {
        this.jwtAccessToken = null;
        this.jwtRefreshToken = null;
        this.jwtExpiresAt = null;
        this.refreshTokenUpdatedAt = null;
    }
}