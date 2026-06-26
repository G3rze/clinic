package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.PrescriptionRequest;
import com.terraplanistas.clinic.domain.dto.response.PrescriptionResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Medicine;
import com.terraplanistas.clinic.domain.entities.Prescription;
import com.terraplanistas.clinic.domain.mapper.PrescriptionMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.repositories.AppointmentRepository;
import com.terraplanistas.clinic.repositories.MedicineRepository;
import com.terraplanistas.clinic.repositories.PrescriptionRepository;
import com.terraplanistas.clinic.services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicineRepository medicineRepository;

    // ------------------------------------------------------------------
    // JC1 — Doctor crea receta con firma SHA-256 generada internamente
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionRequest request, UUID doctorUserId) {

        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Solo el doctor asignado a la cita puede emitir recetas
        if (appointment.getEmployee() == null
                || appointment.getEmployee().getUser() == null
                || !appointment.getEmployee().getUser().getId().equals(doctorUserId)) {
            throw new BusinessRuleException(
                    "Only the assigned doctor can create prescriptions for this appointment");
        }

        Medicine medicine = medicineRepository.findById(request.medicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));

        // Firma SHA-256: hash(appointmentId|medicineId|dosageInstructions|doctorUserId)
        String signatureInput = request.appointmentId() + "|"
                + request.medicineId() + "|"
                + request.dosageInstructions() + "|"
                + doctorUserId;
        String digitalSignature = sha256Hex(signatureInput);

        // Snapshot inmutable del medicamento al momento de la prescripción
        Map<String, Object> medicineSnapshot = Map.of(
                "id",          medicine.getId().toString(),
                "brandName",   medicine.getBrandName(),
                "genericName", medicine.getGenericName() != null ? medicine.getGenericName() : "",
                "atcCode",     medicine.getAtcCode() != null ? medicine.getAtcCode() : ""
        );

        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setMedicine(medicine);
        prescription.setMedicineSnapshot(medicineSnapshot);
        prescription.setDosageInstructions(request.dosageInstructions());
        prescription.setDigitalSignature(digitalSignature);
        prescription.setUsageCount(0);
        prescription.setMaxUsages(request.maxUsages() != null ? request.maxUsages() : 3);
        prescription.setCreatedBy(doctorUserId);

        return PrescriptionMapper.toResponse(prescriptionRepository.save(prescription));
    }

    // ------------------------------------------------------------------
    // JC2 — Dispensar receta (máximo 3 dispensaciones)
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public PrescriptionResponse dispensePrescription(UUID prescriptionId) {

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        if (prescription.getUsageCount() >= prescription.getMaxUsages()) {
            throw new BusinessRuleException(
                    "Prescription has reached its maximum dispensations (" + prescription.getMaxUsages() + ")");
        }

        prescription.setUsageCount(prescription.getUsageCount() + 1);
        return PrescriptionMapper.toResponse(prescriptionRepository.save(prescription));
    }

    // ------------------------------------------------------------------
    // JC3 / JC6 — Paciente ve todas sus recetas
    // ------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsByPatient(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .flatMap(a -> prescriptionRepository.findByAppointmentId(a.getId()).stream())
                .map(PrescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // JC6 — Ver / descargar una receta individual
    // ------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(UUID prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .map(PrescriptionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsByAppointment(UUID appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId)
                .stream()
                .map(PrescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Utilidad SHA-256
    // ------------------------------------------------------------------
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
