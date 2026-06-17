package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.response.PermanentlyAnonymizedAuditResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface UserAnonymizationService {

    void anonymizeUser(UUID userId, UUID performedBy);

    void markAnonymizationPermanent(int days);

    boolean isUserAnonymized(UUID userId);

    Page<PermanentlyAnonymizedAuditResponse> getPermanentlyAnonymized(
            Pageable pageable,
            OffsetDateTime deletedAtFrom,
            OffsetDateTime deletedAtTo,
            OffsetDateTime permanentAtFrom,
            OffsetDateTime permanentAtTo);
}
