package com.terraplanistas.clinic.schedulers;

import com.terraplanistas.clinic.http.security.SecurityProperties;
import com.terraplanistas.clinic.services.UserAnonymizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PurgeAnonymizedUsersScheduler {

    private static final Logger log = LoggerFactory.getLogger(PurgeAnonymizedUsersScheduler.class);

    private final UserAnonymizationService anonymizationService;
    private final SecurityProperties securityProperties;

    public PurgeAnonymizedUsersScheduler(UserAnonymizationService anonymizationService,
                                         SecurityProperties securityProperties) {
        this.anonymizationService = anonymizationService;
        this.securityProperties = securityProperties;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void markAnonymizationPermanent() {
        int retentionDays = securityProperties.getUser().getAnonymizationRetentionDays();
        log.info("Starting to mark anonymized users older than {} days as permanent", retentionDays);

        try {
            anonymizationService.markAnonymizationPermanent(retentionDays);
            log.info("Completed marking anonymized users as permanent");
        } catch (Exception e) {
            log.error("Error marking anonymized users as permanent", e);
        }
    }
}
