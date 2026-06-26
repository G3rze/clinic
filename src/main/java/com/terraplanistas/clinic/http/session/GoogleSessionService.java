package com.terraplanistas.clinic.http.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleSessionService {

    private static final Logger log = LoggerFactory.getLogger(GoogleSessionService.class);
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final GoogleSessionRepository sessionRepository;

    public GoogleSessionService(GoogleSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public GoogleSession createSession(String googleUserId, String accessToken, String refreshToken,
                                       Long expiresInSeconds, String email) {
        Optional<GoogleSession> existingSession = sessionRepository.findByGoogleUserId(googleUserId);

        GoogleSession session;
        if (existingSession.isPresent()) {
            session = existingSession.get();
        } else {
            session = new GoogleSession();
            session.setGoogleUserId(googleUserId);
            session.setCreatedBy(SYSTEM_USER_ID);
        }

        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(expiresInSeconds != null
                ? OffsetDateTime.now().plusSeconds(expiresInSeconds)
                : null);
        session.setGoogleEmail(email);
        session.setUpdatedBy(SYSTEM_USER_ID);

        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public Optional<GoogleSession> getSessionByGoogleUserId(String googleUserId) {
        try {
            return sessionRepository.findActiveSessionByGoogleUserId(googleUserId, OffsetDateTime.now());
        } catch (Exception e) {
            if (isAttributeConverterException(e)) {
                log.warn("Corrupted GoogleSession detected for googleUserId={}. Deleting and returning empty.", googleUserId);
                deleteSessionByGoogleUserId(googleUserId);
                return Optional.empty();
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Optional<GoogleSession> getSessionByUserId(UUID userId) {
        try {
            return sessionRepository.findActiveSessionByUserId(userId, OffsetDateTime.now());
        } catch (Exception e) {
            if (isAttributeConverterException(e)) {
                log.warn("Corrupted GoogleSession detected for userId={}. Deleting and returning empty.", userId);
                deleteSessionByUserId(userId);
                return Optional.empty();
            }
            throw e;
        }
    }

    private boolean isAttributeConverterException(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        Throwable cause = e.getCause();
        String causeMsg = cause != null ? cause.getMessage() != null ? cause.getMessage() : "" : "";
        return msg.contains("AttributeConverter") || causeMsg.contains("AttributeConverter")
                || msg.contains("Error attempting to apply") || causeMsg.contains("decrypt");
    }

    @Transactional
    public GoogleSession updateToken(String googleUserId, String accessToken, String refreshToken,
                                     Long expiresInSeconds) {
        GoogleSession session = sessionRepository.findByGoogleUserId(googleUserId)
                .orElseThrow(() -> new IllegalStateException("Session not found for: " + googleUserId));

        session.setAccessToken(accessToken);
        if (refreshToken != null) {
            session.setRefreshToken(refreshToken);
        }
        session.setExpiresAt(OffsetDateTime.now().plusSeconds(expiresInSeconds));
        session.setUpdatedBy(SYSTEM_USER_ID);

        return sessionRepository.save(session);
    }

    @Transactional
    public void deleteSessionByGoogleUserId(String googleUserId) {
        sessionRepository.deleteByGoogleUserId(googleUserId);
    }

    @Transactional
    public void deleteSessionByUserId(UUID userId) {
        sessionRepository.findByUserId(userId).ifPresent(session -> {
            sessionRepository.delete(session);
        });
    }

    @Transactional
    public void linkUserToSession(String googleUserId, UUID userId) {
        GoogleSession session = sessionRepository.findByGoogleUserId(googleUserId)
                .orElseThrow(() -> new IllegalStateException("Session not found for: " + googleUserId));
        session.setUserId(userId);
        session.setUpdatedBy(SYSTEM_USER_ID);
        sessionRepository.save(session);
    }

    @Transactional
    public int cleanupExpiredSessions() {
        List<GoogleSession> expiredSessions = sessionRepository.findByExpiresAtBefore(OffsetDateTime.now());
        int count = expiredSessions.size();
        sessionRepository.deleteAll(expiredSessions);
        return count;
    }

    @Transactional(readOnly = true)
    public boolean isSessionExpired(String googleUserId) {
        return sessionRepository.findByGoogleUserId(googleUserId)
                .map(GoogleSession::isExpired)
                .orElse(true);
    }

    @Transactional
    public GoogleSession createOrUpdateSessionWithJwt(String googleUserId, String accessToken,
                                                       String refreshToken, Long expiresInSeconds,
                                                       String email, String jwtAccessToken,
                                                       String jwtRefreshToken, OffsetDateTime jwtExpiresAt) {
        Optional<GoogleSession> existingSession = sessionRepository.findByGoogleUserId(googleUserId);

        GoogleSession session;
        if (existingSession.isPresent()) {
            session = existingSession.get();
        } else {
            session = new GoogleSession();
            session.setGoogleUserId(googleUserId);
            session.setCreatedBy(SYSTEM_USER_ID);
        }

        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(OffsetDateTime.now().plusSeconds(expiresInSeconds));
        session.setGoogleEmail(email);
        session.setJwtAccessToken(jwtAccessToken);
        session.setJwtRefreshToken(jwtRefreshToken);
        session.setJwtExpiresAt(jwtExpiresAt);
        session.setRefreshTokenUpdatedAt(OffsetDateTime.now());
        session.setUpdatedBy(SYSTEM_USER_ID);

        return sessionRepository.save(session);
    }

    @Transactional
    public void clearJwtTokensByUserId(UUID userId) {
        sessionRepository.findByUserId(userId).ifPresent(session -> {
            session.clearJwtTokens();
            session.setUpdatedBy(SYSTEM_USER_ID);
            sessionRepository.save(session);
        });
    }

    @Transactional(readOnly = true)
    public Optional<GoogleSession> findActiveJwtSessionByUserId(UUID userId) {
        return sessionRepository.findActiveJwtSessionByUserId(userId, OffsetDateTime.now());
    }

    @Transactional
    public GoogleSession updateJwtTokens(UUID userId, String jwtAccessToken, String jwtRefreshToken,
                                          OffsetDateTime jwtExpiresAt) {
        GoogleSession session = sessionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Session not found for user: " + userId));

        session.setJwtAccessToken(jwtAccessToken);
        session.setJwtRefreshToken(jwtRefreshToken);
        session.setJwtExpiresAt(jwtExpiresAt);
        session.setRefreshTokenUpdatedAt(OffsetDateTime.now());
        session.setUpdatedBy(SYSTEM_USER_ID);

        return sessionRepository.save(session);
    }

    @Transactional
    public void clearJwtTokensByGoogleUserId(String googleUserId) {
        sessionRepository.findByGoogleUserId(googleUserId).ifPresent(session -> {
            session.clearJwtTokens();
            session.setUpdatedBy(SYSTEM_USER_ID);
            sessionRepository.save(session);
        });
    }
}