package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.MedicalRecordRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicalRecordResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.MedicalRecord;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.mapper.MedicalRecordMapper;
import com.terraplanistas.clinic.exceptions.BusinessRuleException;
import com.terraplanistas.clinic.exceptions.ResourceNotFoundException;
import com.terraplanistas.clinic.repositories.AppointmentRepository;
import com.terraplanistas.clinic.repositories.EmployeeRepository;
import com.terraplanistas.clinic.repositories.MedicalRecordRepository;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.services.MedicalRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

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

    @Override
    @Transactional
    public MedicalRecordResponse registerRecord(UUID doctor, MedicalRecordRequest request) {
        if (!doctor.equals(request.employeeId())) {
            throw new BusinessRuleException("Doctor must be the one to create the medical record");
        }

        Optional<Patient> patient = patientRepository.findById(request.patientId());

        if (patient.isEmpty()){
            throw new ResourceAccessException("Patient not found");
        }

        Optional<Employee> employee = employeeRepository.findById(request.employeeId());

        if (employee.isEmpty()){
            throw new ResourceNotFoundException("Employee not found");
        }

        Optional<Appointment> appointment = appointmentRepository.findById(request.appointmentId());

        if (appointment.isEmpty()) {
            throw new ResourceNotFoundException(("Appointment not found"));
        }

        MedicalRecord record = recordRepository.save(
                MedicalRecordMapper.toEntity(
                        request,
                        patient.get(),
                        appointment.get(),
                        employee.get()
                )
        );

        return MedicalRecordMapper.toResponse(record);
    }

    @Override
    public List<MedicalRecordResponse> getFromAppointmentId(UUID requester, UUID appointmentId) {


        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ResourceNotFoundException("Appointment not found")
        );

        if (!requester.equals(appointment.getPatient().getId()) && !requester.equals(appointment.getEmployee().getId())) {
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
}
