package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.UpdateEmployeeSpecialtyRequest;
import com.terraplanistas.clinic.domain.dto.response.DoctorDetailResponse;
import com.terraplanistas.clinic.domain.dto.response.EmployeeSpecialtyResponse;
import com.terraplanistas.clinic.domain.dto.response.PublicDoctorResponse;
import com.terraplanistas.clinic.domain.dto.response.PublicSpecialtyResponse;
import com.terraplanistas.clinic.domain.dto.response.PublicAvailabilityResponse;
import com.terraplanistas.clinic.domain.entities.*;
import com.terraplanistas.clinic.repositories.*;
import com.terraplanistas.clinic.services.EmployeeSpecialtyService;
import com.terraplanistas.clinic.services.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeSpecialtyServiceImpl implements EmployeeSpecialtyService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeSpecialtyRepository employeeSpecialtyRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final SpecialtyRepository specialtyRepository;
    private final EmployeeService employeeService;

    public EmployeeSpecialtyServiceImpl(EmployeeRepository employeeRepository,
                                       EmployeeSpecialtyRepository employeeSpecialtyRepository,
                                       DoctorAvailabilityRepository availabilityRepository,
                                       SpecialtyRepository specialtyRepository,
                                       EmployeeService employeeService) {
        this.employeeRepository = employeeRepository;
        this.employeeSpecialtyRepository = employeeSpecialtyRepository;
        this.availabilityRepository = availabilityRepository;
        this.specialtyRepository = specialtyRepository;
        this.employeeService = employeeService;
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDetailResponse getDoctorDetail(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + employeeId));

        User user = employee.getUser();

        List<EmployeeSpecialty> employeeSpecialties = employeeSpecialtyRepository
                .findByEmployeeIdWithSpecialty(employeeId);

        List<DoctorDetailResponse.SpecialtyDetail> specialtyDetails = employeeSpecialties.stream()
                .map(es -> {
                    List<DoctorAvailability> availabilities = availabilityRepository
                            .findByEmployeeIdAndSpecialtyIdAndIsActiveTrue(employeeId, es.getSpecialtyId());

                    List<DoctorDetailResponse.AvailabilitySlot> slots = availabilities.stream()
                            .map(da -> new DoctorDetailResponse.AvailabilitySlot(
                                    da.getId(),
                                    da.getDayOfWeek(),
                                    da.getStartTime(),
                                    da.getEndTime()
                            )).collect(Collectors.toList());

                    return new DoctorDetailResponse.SpecialtyDetail(
                            es.getSpecialtyId(),
                            es.getSpecialty().getCode(),
                            es.getSpecialty().getName(),
                            es.getProfessionalLicenseNumber(),
                            es.getFeePerHour(),
                            es.getConsultDurationMinutes(),
                            slots
                    );
                }).collect(Collectors.toList());

        String status;
        if (user.isAccessRevoked() || user.getDeletedAt() != null) {
            status = "revoked";
        } else {
            status = "active";
        }

        return new DoctorDetailResponse(
                employee.getId(),
                user.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                user.getEmail(),
                user.getRole().getCode(),
                status,
                employee.getPhones(),
                employee.getAddress(),
                specialtyDetails
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSpecialtyResponse> getSpecialtiesByEmployeeId(UUID employeeId) {
        List<EmployeeSpecialty> employeeSpecialties = employeeSpecialtyRepository
                .findByEmployeeIdWithSpecialty(employeeId);

        return employeeSpecialties.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployeeSpecialtyResponse addSpecialtyToEmployee(UUID employeeId, UUID specialtyId, String professionalLicenseNumber) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + employeeId));

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new EntityNotFoundException("Specialty not found: " + specialtyId));

        EmployeeSpecialtyId id = new EmployeeSpecialtyId(employeeId, specialtyId);
        if (employeeSpecialtyRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee already has this specialty");
        }

        EmployeeSpecialty employeeSpecialty = new EmployeeSpecialty();
        employeeSpecialty.setEmployeeId(employeeId);
        employeeSpecialty.setSpecialtyId(specialtyId);
        employeeSpecialty.setSpecialty(specialty);
        employeeSpecialty.setEmployee(employee);
        employeeSpecialty.setProfessionalLicenseNumber(professionalLicenseNumber);
        employeeSpecialty.setFeePerHour(BigDecimal.ZERO);
        employeeSpecialty.setConsultDurationMinutes(60);
        employeeSpecialty.setShift(new java.util.HashMap<>());

        EmployeeSpecialty saved = employeeSpecialtyRepository.save(employeeSpecialty);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeSpecialtyResponse updateEmployeeSpecialty(UUID employeeId, UUID specialtyId, UpdateEmployeeSpecialtyRequest request) {
        EmployeeSpecialtyId id = new EmployeeSpecialtyId(employeeId, specialtyId);
        EmployeeSpecialty employeeSpecialty = employeeSpecialtyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee specialty not found"));

        employeeSpecialty.setProfessionalLicenseNumber(request.professionalLicenseNumber());
        employeeSpecialty.setFeePerHour(request.feePerHour());
        employeeSpecialty.setConsultDurationMinutes(request.consultDurationMinutes());

        EmployeeSpecialty saved = employeeSpecialtyRepository.save(employeeSpecialty);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void removeSpecialtyFromEmployee(UUID employeeId, UUID specialtyId) {
        EmployeeSpecialtyId id = new EmployeeSpecialtyId(employeeId, specialtyId);
        EmployeeSpecialty employeeSpecialty = employeeSpecialtyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee specialty not found"));

        employeeSpecialtyRepository.delete(employeeSpecialty);
    }

    private EmployeeSpecialtyResponse toResponse(EmployeeSpecialty es) {
        return new EmployeeSpecialtyResponse(
                es.getEmployeeId(),
                es.getSpecialtyId(),
                es.getProfessionalLicenseNumber(),
                es.getFeePerHour(),
                es.getShift(),
                es.getConsultDurationMinutes()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PublicDoctorResponse> getPublicDoctors(String name, String specialty, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Employee> doctorPage;

        if (name != null && !name.isBlank()) {
            doctorPage = employeeRepository.findActiveDoctorsByName(name, pageable);
        } else {
            doctorPage = employeeRepository.findActiveDoctors(pageable);
        }

        List<PublicDoctorResponse> content;

        if (specialty != null && !specialty.isBlank()) {
            String specialtyLower = specialty.toLowerCase();
            List<Employee> filtered = doctorPage.getContent().stream()
                    .filter(d -> {
                        List<EmployeeSpecialty> specialties = employeeSpecialtyRepository
                                .findByEmployeeIdWithSpecialty(d.getId());
                        return specialties.stream().anyMatch(es ->
                                es.getSpecialty().getName().toLowerCase().contains(specialtyLower) ||
                                es.getSpecialty().getCode().toLowerCase().contains(specialtyLower));
                    })
                    .collect(Collectors.toList());
            content = filtered.stream()
                    .map(d -> toPublicResponse(d.getId(), d.getUser().getRole().getCode()))
                    .collect(Collectors.toList());
        } else {
            content = doctorPage.getContent().stream()
                    .map(d -> toPublicResponse(d.getId(), d.getUser().getRole().getCode()))
                    .collect(Collectors.toList());
        }

        return new org.springframework.data.domain.PageImpl<>(content, pageable, doctorPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicDoctorResponse getPublicDoctorDetail(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + employeeId));

        if (!employee.getIsActive() ||
            employee.getUser().isAccessRevoked() ||
            employee.getUser().getDeletedAt() != null ||
            employee.getUser().getGoogleUserId() == null) {
            throw new EntityNotFoundException("Doctor not found: " + employeeId);
        }

        return toPublicResponse(employeeId, employee.getUser().getRole().getCode());
    }

    private PublicDoctorResponse toPublicResponse(UUID employeeId, String roleCode) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        List<EmployeeSpecialty> employeeSpecialties = employeeSpecialtyRepository
                .findByEmployeeIdWithSpecialty(employeeId);

        List<PublicSpecialtyResponse> specialties = employeeSpecialties.stream()
                .map(es -> {
                    List<DoctorAvailability> availabilities = availabilityRepository
                            .findByEmployeeIdAndSpecialtyIdAndIsActiveTrue(employeeId, es.getSpecialtyId());

                    List<PublicAvailabilityResponse> slots = availabilities.stream()
                            .map(da -> new PublicAvailabilityResponse(
                                    da.getDayOfWeek(),
                                    da.getStartTime().toString(),
                                    da.getEndTime().toString()
                            )).collect(Collectors.toList());

                    return new PublicSpecialtyResponse(
                            es.getSpecialtyId(),
                            es.getSpecialty().getCode(),
                            es.getSpecialty().getName(),
                            es.getProfessionalLicenseNumber(),
                            es.getFeePerHour(),
                            es.getConsultDurationMinutes(),
                            slots
                    );
                }).collect(Collectors.toList());

        return new PublicDoctorResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                roleCode,
                specialties
        );
    }
}
