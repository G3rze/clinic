package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.response.PermanentlyAnonymizedAuditResponse;
import com.terraplanistas.clinic.services.UserAnonymizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("${app.base-uri}/admin/audits")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserAnonymizationService anonymizationService;

    public AdminAuditController(UserAnonymizationService anonymizationService) {
        this.anonymizationService = anonymizationService;
    }

    @GetMapping("/permanently-anonymized")
    public ResponseEntity<Page<PermanentlyAnonymizedAuditResponse>> getPermanentlyAnonymized(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime deletedAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime deletedAtTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime permanentAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime permanentAtTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "anonymizationPermanentAt,desc") String sort) {

        int pageSize = Math.min(size > 0 ? size : DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        Pageable pageable = createPageable(page, pageSize, sort);

        Page<PermanentlyAnonymizedAuditResponse> result = anonymizationService.getPermanentlyAnonymized(
                pageable, deletedAtFrom, deletedAtTo, permanentAtFrom, permanentAtTo);

        return ResponseEntity.ok(result);
    }

    private Pageable createPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String field = parts[0];
        org.springframework.data.domain.Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? org.springframework.data.domain.Sort.Direction.ASC
                : org.springframework.data.domain.Sort.Direction.DESC;
        return PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, field));
    }
}
