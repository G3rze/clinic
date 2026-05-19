package com.terraplanistas.clinic.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "employeesxspecialties", schema = "clinic")
@IdClass(EmployeeSpecialtyId.class)
@Getter
@Setter
public class EmployeeSpecialty {

    @Id
    @Column(name = "employee_id")
    private UUID employeeId;

    @Id
    @Column(name = "specialty_id")
    private UUID specialtyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", insertable = false, updatable = false)
    private Specialty specialty;

    @Column(name = "professional_license_number", nullable = false, unique = true)
    private String professionalLicenseNumber;

    @Column(name = "fee_per_hour", nullable = false)
    private BigDecimal feePerHour = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shift", nullable = false)
    private Map<String, Object> shift;
}