package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.PostAppointmentNoteRequest;
import com.terraplanistas.clinic.domain.dto.response.PostAppointmentNoteResponse;
import com.terraplanistas.clinic.services.PostAppointmentNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/post-appointment-note")
@RequiredArgsConstructor
public class PostAppointmentNoteController {

    private final PostAppointmentNoteService service;

    @PostMapping
    public ResponseEntity<PostAppointmentNoteResponse> createNote(Authentication authentication, @RequestBody @Valid PostAppointmentNoteRequest note){
        UUID doctor = (UUID) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.createNote(doctor, note)
        );
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<List<PostAppointmentNoteResponse>> getNoteFromAppointment(Authentication authentication, @PathVariable UUID appointmentId){
        UUID requester = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok().body(
          service.getNoteFromAppointment(requester, appointmentId)
        );
    }
}
