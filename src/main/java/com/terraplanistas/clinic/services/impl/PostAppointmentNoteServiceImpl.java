package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.PostAppointmentNoteRequest;
import com.terraplanistas.clinic.domain.dto.response.PostAppointmentNoteResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.PostAppointmentNote;
import com.terraplanistas.clinic.domain.mapper.PostAppointmentNoteMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.repositories.AppointmentRepository;
import com.terraplanistas.clinic.repositories.PostAppointmentNoteRepository;
import com.terraplanistas.clinic.services.PostAppointmentNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostAppointmentNoteServiceImpl implements PostAppointmentNoteService {
    private final PostAppointmentNoteRepository postAppointmentNoteRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public PostAppointmentNoteResponse createNote(UUID doctor, PostAppointmentNoteRequest note) {
        Appointment appointment = appointmentRepository.findById(note.appointmentId()).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!doctor.equals(appointment.getEmployee().getId())){
            throw new BusinessRuleException("Only doctor can write post appointment note");
        }

        PostAppointmentNote note_e = postAppointmentNoteRepository.save(PostAppointmentNoteMapper.toEntity(note, appointment));
        return PostAppointmentNoteMapper.toResponse(note_e);
    }

    @Override
    public List<PostAppointmentNoteResponse> getNoteFromAppointment(UUID requester, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        if (!requester.equals(appointment.getEmployee().getId()) && !requester.equals(appointment.getPatient().getId())){
            throw new BusinessRuleException("Only doctor and patient can read the post appointment note");
        }

        List<PostAppointmentNote> notes = postAppointmentNoteRepository.findByAppointmentId(appointmentId);

        return notes.stream().map(
                PostAppointmentNoteMapper::toResponse
        ).collect(Collectors.toList());
    }
}
