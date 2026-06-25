package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.CreateEmployeeRequest;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.Role;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.domain.encryption.AESEncryptionService;
import com.terraplanistas.clinic.http.security.EmailDomainValidator;
import com.terraplanistas.clinic.repositories.EmployeeRepository;
import com.terraplanistas.clinic.repositories.RoleRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import com.terraplanistas.clinic.services.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String EMPLOYEE_ROLE_CODE = "EMPLOYEE";
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailDomainValidator emailDomainValidator;
    private final AESEncryptionService encryptionService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               EmailDomainValidator emailDomainValidator,
                               AESEncryptionService encryptionService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailDomainValidator = emailDomainValidator;
        this.encryptionService = encryptionService;
    }

    @Override
    @Transactional
    public User createEmployee(CreateEmployeeRequest request, UUID currentUserId) {
        if (!emailDomainValidator.isEmployeeEmail(request.email())) {
            throw new IllegalArgumentException("Email must be from organization domain");
        }

        String emailBindex = encryptionService.encryptDeterministic(request.email().toLowerCase());
        if (userRepository.findByEmailBindex(emailBindex).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        String roleCode = request.role() != null ? request.role() : EMPLOYEE_ROLE_CODE;

        if ("ADMIN".equals(roleCode)) {
            long activeAdminCount = userRepository.countByRoleCodeAndAccessRevokedFalse("ADMIN");
            if (activeAdminCount >= 2) {
                throw new IllegalArgumentException("Maximum admin accounts reached. Cannot create more admins.");
            }
        }

        Role targetRole = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleCode));

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.firstName() + " " + request.lastName());
        user.setRole(targetRole);
        user.setCreatedBy(currentUserId);
        user.setUpdatedBy(currentUserId);
        user.setEmailBindex(encryptionService.encryptDeterministic(request.email().toLowerCase()));
        user.setUsernameBindex(encryptionService.encryptDeterministic((request.firstName() + " " + request.lastName()).toLowerCase()));
        user = userRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setIdNumber(request.idNumber());
        employee.setIdType(request.idType());
        employee.setAddress(request.address() != null ? request.address() : "");
        employee.setPhones(request.phones() != null ? request.phones() : "");
        employee.setIsActive(true);
        employee.setCreatedBy(currentUserId);
        employee.setUpdatedBy(currentUserId);
        employee.setIdNumberBindex(encryptionService.encryptDeterministic(request.idNumber()));

        employeeRepository.save(employee);

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getActiveEmployees() {
        return employeeRepository.findActiveWithNonRevokedUser();
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeByUserId(UUID userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found for user: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmployeeId(UUID employeeId) {
        Employee employee = getEmployeeById(employeeId);
        return employee.getUser();
    }

    @Override
    @Transactional
    public Employee updateEmployee(UUID id, CreateEmployeeRequest request) {
        Employee employee = getEmployeeById(id);
        User user = employee.getUser();

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (!emailDomainValidator.isEmployeeEmail(request.email())) {
                throw new IllegalArgumentException("Email must be from organization domain");
            }
            user.setEmail(request.email());
        }

        if (request.firstName() != null) {
            employee.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            employee.setLastName(request.lastName());
        }
        if (request.idNumber() != null) {
            employee.setIdNumber(request.idNumber());
        }
        if (request.idType() != null) {
            employee.setIdType(request.idType());
        }
        if (request.address() != null) {
            employee.setAddress(request.address());
        }
        if (request.phones() != null) {
            employee.setPhones(request.phones());
        }

        employee.setUpdatedBy(SYSTEM_USER_ID);
        user.setUpdatedBy(SYSTEM_USER_ID);

        userRepository.save(user);
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void revokeEmployeeAccess(UUID employeeId, UUID currentAdminUserId) {
        Employee employee = getEmployeeById(employeeId);
        User user = employee.getUser();

        if (user.getId().equals(currentAdminUserId)) {
            throw new IllegalArgumentException("Cannot revoke your own account");
        }

        if ("ADMIN".equals(user.getRole().getCode())) {
            long activeAdminCount = userRepository.countByRoleCodeAndAccessRevokedFalse("ADMIN");
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot revoke the last admin account");
            }
        }

        user.setAccessRevoked(true);
        user.setDeletedAt(OffsetDateTime.now());
        user.setUpdatedBy(SYSTEM_USER_ID);

        userRepository.save(user);
    }

    @Transactional
    public void revokeEmployeeAccess(UUID employeeId) {
        revokeEmployeeAccess(employeeId, null);
    }

    @Override
    @Transactional
    public void reactivateEmployee(UUID employeeId) {
        Employee employee = getEmployeeById(employeeId);
        User user = employee.getUser();

        user.setAccessRevoked(false);
        user.setDeletedAt(null);
        user.setUpdatedBy(SYSTEM_USER_ID);

        userRepository.save(user);
    }
}
