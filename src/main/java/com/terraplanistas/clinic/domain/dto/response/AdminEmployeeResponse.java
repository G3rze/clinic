package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.domain.enums.IdType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminEmployeeResponse(
    UUID id,
    String firstName,
    String lastName,
    String idNumber,
    IdType idType,
    String address,
    String phones,
    Boolean isActive,
    UUID userId,
    String email,
    String status,
    OffsetDateTime deletedAt
) {
    public static AdminEmployeeResponse from(Employee employee) {
        User user = employee.getUser();
        String status;
        if (user.isAccessRevoked() || user.getDeletedAt() != null) {
            status = "revoked";
        } else {
            status = "active";
        }

        return new AdminEmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getIdNumber(),
                employee.getIdType(),
                employee.getAddress(),
                employee.getPhones(),
                employee.getIsActive(),
                user.getId(),
                user.getEmail(),
                status,
                user.getDeletedAt()
        );
    }
}