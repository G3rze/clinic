package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.PostAppointmentNoteRequest;
import com.terraplanistas.clinic.domain.dto.response.PostAppointmentNoteResponse;
import com.terraplanistas.clinic.domain.entities.PostAppointmentNote;
import com.terraplanistas.clinic.domain.entities.Appointment;

public class PostAppointmentNoteMapper {

    public static PostAppointmentNote toEntity(PostAppointmentNoteRequest request, Appointment appointment) {
        PostAppointmentNote note = new PostAppointmentNote();
        note.setAppointment(appointment);
        note.setContent(request.content());
        return note;
    }

    public static PostAppointmentNote toUpgrade(PostAppointmentNoteRequest request, PostAppointmentNote note) {
        note.setContent(request.content());
        return note;
    }

    public static PostAppointmentNoteResponse toResponse(PostAppointmentNote note) {
        return new PostAppointmentNoteResponse(
            note.getId(),
            note.getAppointment() != null ? note.getAppointment().getId() : null,
            note.getContent()
        );
    }
}