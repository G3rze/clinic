package com.terraplanistas.clinic.http.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;

@Component
public class GoogleSessionCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(GoogleSessionCleanupScheduler.class);

    private final GoogleSessionService sessionService;

    public GoogleSessionCleanupScheduler(GoogleSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Scheduled(fixedRateString = "${google.session.cleanup-interval-ms:1800000}")
    public void cleanupExpiredSessions() {
        log.info("Starting expired Google sessions cleanup at {}", OffsetDateTime.now());
        try {
            int deletedCount = sessionService.cleanupExpiredSessions();
            log.info("Expired Google sessions cleanup completed. Deleted {} sessions", deletedCount);
        } catch (Exception e) {
            log.error("Error during expired Google sessions cleanup", e);
        }
    }
}