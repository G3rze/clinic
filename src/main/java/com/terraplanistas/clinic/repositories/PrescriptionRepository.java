package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    List<Prescription> findByAppointmentId(UUID appointmentId);
    List<Prescription> findByMedicineId(UUID medicineId);
}