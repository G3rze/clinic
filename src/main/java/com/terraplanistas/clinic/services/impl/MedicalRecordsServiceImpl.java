package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.MedicalRecordRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicalRecordResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.MedicalRecord;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.domain.mapper.MedicalRecordMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.repositories.AppointmentRepository;
import com.terraplanistas.clinic.repositories.EmployeeRepository;
import com.terraplanistas.clinic.repositories.MedicalRecordRepository;
import com.terraplanistas.clinic.repositories.PatientRepresentativeRepository;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.services.MedicalRecordService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordsServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final EmployeeRepository employeeRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepresentativeRepository patientRepresentativeRepository;

    @Override
    @Transactional
    public MedicalRecordResponse registerRecord(UUID doctor, MedicalRecordRequest request) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(request.appointmentId());

        if (appointmentOpt.isEmpty()) {
            throw new ResourceNotFoundException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();

        if (appointment.getEmployee() == null || appointment.getEmployee().getUser() == null
                || !appointment.getEmployee().getUser().getId().equals(doctor)) {
            throw new BusinessRuleException("Doctor must be the one to create the medical record");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED
                && appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Cannot complete an appointment that is not SCHEDULED or IN_PROGRESS");
        }

        Optional<Patient> patient = patientRepository.findById(request.patientId());

        if (patient.isEmpty()){
            throw new ResourceAccessException("Patient not found");
        }

        Optional<Employee> employee = employeeRepository.findById(request.employeeId());

        if (employee.isEmpty()){
            throw new ResourceNotFoundException("Employee not found");
        }

        MedicalRecord record = recordRepository.save(
                MedicalRecordMapper.toEntity(
                        request,
                        patient.get(),
                        appointment,
                        employee.get(),
                        doctor
                )
        );

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return MedicalRecordMapper.toResponse(record);
    }

    @Override
    public List<MedicalRecordResponse> getFromAppointmentId(UUID requester, UUID appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ResourceNotFoundException("Appointment not found")
        );

        boolean isPatient = appointment.getPatient() != null
                && appointment.getPatient().getUser() != null
                && requester.equals(appointment.getPatient().getUser().getId());

        boolean isDoctor = appointment.getEmployee() != null
                && appointment.getEmployee().getUser() != null
                && requester.equals(appointment.getEmployee().getUser().getId());

        boolean isRepresentative = appointment.getPatient() != null
                && !patientRepresentativeRepository.findByPatientIdAndRepresentativeUserIdAndDeletedAtIsNull(
                        appointment.getPatient().getId(), requester).isEmpty();

        if (!isPatient && !isDoctor && !isRepresentative) {
            throw new BusinessRuleException("Only patient or doctor must access medical record");
        }

        List<MedicalRecord> records = recordRepository.findByAppointmentId(appointmentId);
        if (records.isEmpty()){
            throw new ResourceNotFoundException("Medical record not found");
        }

        return records.stream().map(
                MedicalRecordMapper::toResponse
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getByPatientId(UUID patientId) {
        List<MedicalRecord> records = recordRepository.findByPatientId(patientId);
        return records.stream()
                .map(MedicalRecordMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getByPatientIdAndFilters(UUID patientId, OffsetDateTime fromDate, OffsetDateTime toDate, UUID doctorId) {
        List<MedicalRecord> records = recordRepository.findByPatientId(patientId);

        return records.stream()
                .filter(r -> {
                    if (fromDate != null && r.getCreatedAt().isBefore(fromDate)) return false;
                    if (toDate != null && r.getCreatedAt().isAfter(toDate)) return false;
                    if (doctorId != null && !doctorId.equals(r.getEmployee().getId())) return false;
                    return true;
                })
                .map(MedicalRecordMapper::toResponse)
                .collect(Collectors.toList());
    }
}
