package com.terraplanistas.clinic.http.security;

import com.terraplanistas.clinic.domain.entities.Role;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.repositories.RoleRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SecurityProperties securityProperties;

    public DataInitializer(RoleRepository roleRepository,
                          UserRepository userRepository,
                          SecurityProperties securityProperties) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.securityProperties = securityProperties;
    }

    @PostConstruct
    @Transactional
    public void initialize() {
        initializeRoles();
        initializeDefaultAdmin();
    }

    private void initializeRoles() {
        createRoleIfNotExists("ADMIN", "Administrator");
        createRoleIfNotExists("EMPLOYEE", "Employee");
        createRoleIfNotExists("USER", "Regular User");
    }

    private void createRoleIfNotExists(String code, String name) {
        if (roleRepository.findByCode(code).isEmpty()) {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setCreatedBy(SYSTEM_USER_ID);
            roleRepository.save(role);
            log.info("Created role: {}", code);
        }
    }

    private void initializeDefaultAdmin() {
        long adminCount = userRepository.countByRoleCode("ADMIN");

        if (adminCount == 0) {
            String adminEmail = "admin@" + securityProperties.getEmployee().getEmailDomain();

            Role adminRole = roleRepository.findByCode("ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setUsername("Administrator");
            admin.setRole(adminRole);
            admin.setCreatedBy(null);
            admin.setUpdatedBy(null);

            userRepository.save(admin);
            log.info("Created default admin user: {}", adminEmail);
        }
    }
}