package com.terraplanistas.clinic.http.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.domain.entities.Role;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.repositories.RoleRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String DEFAULT_ROLE_CODE = "USER";

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final EmailDomainValidator emailDomainValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OAuth2AuthenticationSuccessHandler(JwtTokenService jwtTokenService,
                                              UserRepository userRepository,
                                              RoleRepository roleRepository,
                                              PatientRepository patientRepository,
                                              EmailDomainValidator emailDomainValidator) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.emailDomainValidator = emailDomainValidator;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();

        String googleUserId = oauth2User.getName();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        EmailDomainValidator.EmailUserType userType = emailDomainValidator.determineUserType(email);

        switch (userType) {
            case EMPLOYEE -> handleEmployeeUser(googleUserId, email, name, oauthToken, response);
            case PATIENT -> handlePatientUser(googleUserId, email, name, oauthToken, response);
            case NOT_ALLOWED -> rejectLogin(response, email, "Email domain not allowed");
        }
    }

    private void handleEmployeeUser(String googleUserId, String email, String name,
                                     OAuth2AuthenticationToken oauthToken,
                                     HttpServletResponse response) throws IOException {
        Optional<User> existingUser = userRepository.findByGoogleUserId(googleUserId);

        if (existingUser.isEmpty()) {
            rejectLogin(response, email, "Employee account must be pre-created by administrator");
            return;
        }

        User user = existingUser.get();

        if (user.getDeletedAt() != null || user.isAccessRevoked()) {
            rejectLogin(response, email, "Employee account has been deactivated");
            return;
        }

        if (!"EMPLOYEE".equals(user.getRole().getCode())) {
            rejectLogin(response, email, "Invalid account type for employee login");
            return;
        }

        generateAndReturnTokens(user, oauthToken, response);
    }

    private void handlePatientUser(String googleUserId, String email, String name,
                                    OAuth2AuthenticationToken oauthToken,
                                    HttpServletResponse response) throws IOException {
        Optional<User> existingUser = userRepository.findByGoogleUserId(googleUserId);

        User user;
        boolean isNewUser = false;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getDeletedAt() != null) {
                rejectLogin(response, email, "Account has been deactivated");
                return;
            }
        } else {
            user = createSkeletonPatientUser(googleUserId, email, name);
            isNewUser = true;
        }

        boolean profileIncomplete = isProfileIncomplete(user);

        generateAndReturnTokens(user, oauthToken, response, isNewUser, profileIncomplete);
    }

    private User createSkeletonPatientUser(String googleUserId, String email, String name) {
        Role userRole = roleRepository.findByCode(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("USER role not found"));

        User user = new User();
        user.setGoogleUserId(googleUserId);
        user.setEmail(email);
        user.setUsername(name != null ? name : "User");
        user.setRole(userRole);
        user.setCreatedBy(getSystemUserId());
        user.setUpdatedBy(getSystemUserId());

        user = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(name != null ? name : "User");
        patient.setLastName("");
        patient.setIsActive(true);
        patient.setCreatedBy(getSystemUserId());
        patient.setUpdatedBy(getSystemUserId());

        patientRepository.save(patient);

        return user;
    }

    private boolean isProfileIncomplete(User user) {
        Optional<Patient> patient = patientRepository.findByUserId(user.getId());
        if (patient.isEmpty()) {
            return true;
        }
        Patient p = patient.get();
        return p.getFirstName() == null || p.getFirstName().isBlank() ||
               p.getLastName() == null || p.getLastName().isBlank() ||
               p.getIdNumber() == null || p.getIdNumber().isBlank() ||
               p.getBirthdate() == null ||
               p.getAddress() == null || p.getAddress().isBlank();
    }

    private void generateAndReturnTokens(User user, OAuth2AuthenticationToken oauthToken,
                                          HttpServletResponse response) throws IOException {
        generateAndReturnTokens(user, oauthToken, response, false, false);
    }

    private void generateAndReturnTokens(User user, OAuth2AuthenticationToken oauthToken,
                                          HttpServletResponse response,
                                          boolean isNewUser, boolean profileIncomplete) throws IOException {
        List<String> roles = oauthToken.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .collect(Collectors.toList());

        String jwtAccessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), roles);
        String jwtRefreshToken = jwtTokenService.generateRefreshToken(user.getId());

        String accountStatus = profileIncomplete ? "profile_incomplete" : "active";
        String requiresAction = profileIncomplete ? "complete_patient_profile" : null;

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", jwtAccessToken);
        tokenResponse.put("refresh_token", jwtRefreshToken);
        tokenResponse.put("token_type", "Bearer");
        tokenResponse.put("expires_in", jwtTokenService.getAccessTokenExpirationMs() / 1000);
        tokenResponse.put("is_new_user", isNewUser);
        tokenResponse.put("user", Map.of(
                "id", user.getId().toString(),
                "google_user_id", user.getGoogleUserId(),
                "name", user.getUsername(),
                "email", user.getEmail(),
                "roles", roles,
                "account_status", accountStatus,
                "requires_action", requiresAction
        ));

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getOutputStream(), tokenResponse);
    }

    private void rejectLogin(HttpServletResponse response, String email, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Login Rejected");
        errorResponse.put("message", message);
        errorResponse.put("email", email);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private UUID getSystemUserId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
