package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AvailableSlotsQuery;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.AvailableSlotResponse;
import com.terraplanistas.clinic.services.AvailableSlotsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/slots")
public class AvailableSlotsController {

    private final AvailableSlotsService availableSlotsService;

    public AvailableSlotsController(AvailableSlotsService availableSlotsService) {
        this.availableSlotsService = availableSlotsService;
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<AvailableSlotResponse>>> findAvailableSlots(
            @RequestParam UUID doctorId,
            @RequestParam String specialtyCode,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Integer consultDurationMinutes,
            @RequestParam(required = false) ZoneId timezone) {
        AvailableSlotsQuery query = new AvailableSlotsQuery(
            doctorId,
            specialtyCode,
            startDate,
            endDate,
            timezone,
            consultDurationMinutes
        );
        List<AvailableSlotResponse> slots = availableSlotsService.findAvailableSlots(query);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }
}
