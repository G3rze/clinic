package com.terraplanistas.clinic.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "oauth_tokens", schema = "clinic")
@Getter
@Setter
public class OAuthToken extends BaseEntity {

    @Column(name = "google_user_id", nullable = false, unique = true)
    private String googleUserId;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "expires_at")
    private Long expiresAt;
}
