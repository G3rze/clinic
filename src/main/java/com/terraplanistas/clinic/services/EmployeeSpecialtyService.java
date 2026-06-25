package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.UpdateEmployeeSpecialtyRequest;
import com.terraplanistas.clinic.domain.dto.response.DoctorDetailResponse;
import com.terraplanistas.clinic.domain.dto.response.EmployeeSpecialtyResponse;
import com.terraplanistas.clinic.domain.dto.response.PublicDoctorResponse;
import com.terraplanistas.clinic.domain.entities.Employee;

import java.util.List;
import java.util.UUID;

public interface EmployeeSpecialtyService {

    DoctorDetailResponse getDoctorDetail(UUID employeeId);

    List<EmployeeSpecialtyResponse> getSpecialtiesByEmployeeId(UUID employeeId);

    EmployeeSpecialtyResponse addSpecialtyToEmployee(UUID employeeId, UUID specialtyId, String professionalLicenseNumber);

    EmployeeSpecialtyResponse updateEmployeeSpecialty(UUID employeeId, UUID specialtyId, UpdateEmployeeSpecialtyRequest request);

    void removeSpecialtyFromEmployee(UUID employeeId, UUID specialtyId);

    org.springframework.data.domain.Page<PublicDoctorResponse> getPublicDoctors(String name, String specialty, org.springframework.data.domain.Pageable pageable);

    PublicDoctorResponse getPublicDoctorDetail(UUID employeeId);
}
