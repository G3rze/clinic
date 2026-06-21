package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.PostAppointmentNoteRequest;
import com.terraplanistas.clinic.domain.dto.response.PostAppointmentNoteResponse;

import java.util.List;
import java.util.UUID;

public interface PostAppointmentNoteService {
    public PostAppointmentNoteResponse createNote(UUID doctor, PostAppointmentNoteRequest note);
    public List<PostAppointmentNoteResponse> getNoteFromAppointment(UUID requester, UUID appointment);
}
