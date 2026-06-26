package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.PrescriptionRequest;
import com.terraplanistas.clinic.domain.dto.response.PrescriptionResponse;

import java.util.List;
import java.util.UUID;

public interface PrescriptionService {

    /** JC1 — Doctor crea receta; firma SHA-256 generada internamente */
    PrescriptionResponse createPrescription(PrescriptionRequest request, UUID doctorUserId);

    /** JC2 — Dispensar receta (máximo 3 usos) */
    PrescriptionResponse dispensePrescription(UUID prescriptionId);

    /** JC3 / JC6 — Paciente ve todas sus recetas */
    List<PrescriptionResponse> getPrescriptionsByPatient(UUID patientId);

    /** JC6 — Ver receta individual */
    PrescriptionResponse getPrescriptionById(UUID prescriptionId);

    /** Recetas de una cita específica */
    List<PrescriptionResponse> getPrescriptionsByAppointment(UUID appointmentId);
}
