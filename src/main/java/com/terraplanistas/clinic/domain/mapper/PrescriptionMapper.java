package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.PrescriptionRequest;
import com.terraplanistas.clinic.domain.dto.response.PrescriptionResponse;
import com.terraplanistas.clinic.domain.entities.Prescription;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Medicine;

public class PrescriptionMapper {

    public static Prescription toEntity(PrescriptionRequest request, Appointment appointment, Medicine medicine) {
        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setMedicine(medicine);
        prescription.setMedicineSnapshot(request.medicineSnapshot());
        prescription.setDosageInstructions(request.dosageInstructions());
        prescription.setDigitalSignature(request.digitalSignature());
        prescription.setUsageCount(request.usageCount());
        prescription.setMaxUsages(request.maxUsages());
        return prescription;
    }

    public static Prescription toUpgrade(PrescriptionRequest request, Prescription prescription) {
        prescription.setMedicineSnapshot(request.medicineSnapshot());
        prescription.setDosageInstructions(request.dosageInstructions());
        prescription.setDigitalSignature(request.digitalSignature());
        prescription.setUsageCount(request.usageCount());
        prescription.setMaxUsages(request.maxUsages());
        return prescription;
    }

    public static PrescriptionResponse toResponse(Prescription prescription) {
        return new PrescriptionResponse(
            prescription.getId(),
            prescription.getAppointment() != null ? prescription.getAppointment().getId() : null,
            prescription.getMedicine() != null ? prescription.getMedicine().getId() : null,
            prescription.getMedicineSnapshot(),
            prescription.getDosageInstructions(),
            prescription.getDigitalSignature(),
            prescription.getUsageCount(),
            prescription.getMaxUsages(),
            prescription.getCreatedAt(),
            prescription.getCreatedBy()
        );
    }
}