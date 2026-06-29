package com.terraplanistas.clinic.http.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terraplanistas.clinic.domain.entities.PendingUserConfig;
import com.terraplanistas.clinic.domain.entities.Role;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.domain.encryption.AESEncryptionService;
import com.terraplanistas.clinic.http.credentials.GoogleTokenService;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.repositories.PendingUserConfigRepository;
import com.terraplanistas.clinic.repositories.RoleRepository;
import com.terraplanistas.clinic.repositories.UserConsentRepository;
import com.terraplanistas.clinic.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    private static final String DEFAULT_ROLE_CODE = "USER";

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PendingUserConfigRepository pendingUserConfigRepository;
    private final EmailDomainValidator emailDomainValidator;
    private final SecurityProperties securityProperties;
    private final AESEncryptionService encryptionService;
    private final GoogleTokenService googleTokenService;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final CookieService cookieService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OAuth2AuthenticationSuccessHandler(JwtTokenService jwtTokenService,
                                              UserRepository userRepository,
                                              RoleRepository roleRepository,
                                              PatientRepository patientRepository,
                                              PendingUserConfigRepository pendingUserConfigRepository,
                                              UserConsentRepository userConsentRepository,
                                              EmailDomainValidator emailDomainValidator,
                                              SecurityProperties securityProperties,
                                              AESEncryptionService encryptionService,
                                              GoogleTokenService googleTokenService,
                                              OAuth2AuthorizedClientRepository authorizedClientRepository,
                                              OAuth2AuthorizedClientService authorizedClientService,
                                              CookieService cookieService) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.pendingUserConfigRepository = pendingUserConfigRepository;
        this.emailDomainValidator = emailDomainValidator;
        this.securityProperties = securityProperties;
        this.encryptionService = encryptionService;
        this.googleTokenService = googleTokenService;
        this.authorizedClientRepository = authorizedClientRepository;
        this.authorizedClientService = authorizedClientService;
        this.cookieService = cookieService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();

            String googleUserId = oauth2User.getName();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            log.info("Google OAuth - googleUserId (sub): {}, email: {}, name: {}", googleUserId, email, name);

            EmailDomainValidator.EmailUserType userType = emailDomainValidator.determineUserType(email);

            switch (userType) {
                case EMPLOYEE -> handleEmployeeUser(googleUserId, email, name, oauthToken, response, request);
                case PATIENT -> handlePatientUser(googleUserId, email, name, oauthToken, response, request);
                case NOT_ALLOWED -> rejectLogin(response, email, "Email domain not allowed");
            }
        } catch (Exception e) {
            log.error("OAuth2 authentication success handler failed", e);
            rejectLogin(response, "unknown", "Authentication processing failed: " + e.getMessage());
        }
    }

    private void handleEmployeeUser(String googleUserId, String email, String name,
                                     OAuth2AuthenticationToken oauthToken,
                                     HttpServletResponse response,
                                     HttpServletRequest request) throws IOException {
        Optional<User> existingUser = userRepository.findByGoogleUserId(googleUserId);

        if (existingUser.isEmpty()) {
            String emailBindex = encryptionService.encryptDeterministic(email.toLowerCase());
            log.debug("Employee lookup by googleUserId failed. Trying email_bindex lookup. email={}, emailBindex={}", email, emailBindex);
            Optional<User> userByEmail = userRepository.findByEmailBindex(emailBindex);
            log.debug("Employee lookup by email_bindex result: found={}", userByEmail.isPresent());
            if (userByEmail.isPresent()) {
                User user = userByEmail.get();
                String roleCode = user.getRole().getCode();
                if (!"EMPLOYEE".equals(roleCode) && !"ADMIN".equals(roleCode)) {
                    rejectLogin(response, email, "Invalid account type for employee login");
                    return;
                }
                if (user.getDeletedAt() != null || user.isAccessRevoked()) {
                    rejectLogin(response, email, "Account has been deactivated");
                    return;
                }

                if ("ADMIN".equals(roleCode)) {
                    long activeAdminCount = userRepository.countByRoleCodeAndAccessRevokedFalse("ADMIN");
                    if (activeAdminCount >= 2) {
                        rejectLogin(response, email, "Maximum admin accounts reached. Contact existing administrator.");
                        return;
                    }
                }

                user.setGoogleUserId(googleUserId);
                userRepository.save(user);
                log.info("Auto-updated google_user_id for employee: {}", email);
                generateAndReturnTokens(user, oauthToken, response);
                return;
            }
            rejectLogin(response, email, "Employee account must be pre-created by administrator");
            return;
        }

        User user = existingUser.get();

        if (user.getDeletedAt() != null || user.isAccessRevoked()) {
            rejectLogin(response, email, "Employee account has been deactivated");
            return;
        }

        String roleCode = user.getRole().getCode();
        if (!"EMPLOYEE".equals(roleCode) && !"ADMIN".equals(roleCode)) {
            rejectLogin(response, email, "Invalid account type for employee login");
            return;
        }

        if ("ADMIN".equals(roleCode)) {
            long activeAdminCount = userRepository.countByRoleCodeAndAccessRevokedFalse("ADMIN");
            if (activeAdminCount >= 2) {
                rejectLogin(response, email, "Maximum admin accounts reached. Contact existing administrator.");
                return;
            }
        }

        generateAndReturnTokens(user, oauthToken, response);
    }

    private void handlePatientUser(String googleUserId, String email, String name,
                                    OAuth2AuthenticationToken oauthToken,
                                    HttpServletResponse response,
                                    HttpServletRequest request) throws IOException {
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
            String emailBindex = encryptionService.encryptDeterministic(email.toLowerCase());
            Optional<User> userByEmail = userRepository.findByEmailBindex(emailBindex);
            if (userByEmail.isPresent()) {
                user = userByEmail.get();
                if (user.getDeletedAt() != null) {
                    rejectLogin(response, email, "Account has been deactivated");
                    return;
                }
                user.setGoogleUserId(googleUserId);
                userRepository.save(user);
                log.info("Auto-updated google_user_id for patient: {}", email);

                if (pendingUserConfigRepository.findByGoogleUserId(googleUserId).isEmpty()) {
                    PendingUserConfig config = new PendingUserConfig();
                    config.setGoogleUserId(googleUserId);
                    config.setEmail(email);
                    config.setName(name);
                    config.setCreatedAt(OffsetDateTime.now());
                    config.setConsentGiven(false);
                    config.setProfileComplete(false);
                    pendingUserConfigRepository.save(config);
                    log.info("Created PendingUserConfig for existing patient user: {}", email);
                }
            } else {
                user = createSkeletonUser(googleUserId, email, name);
                isNewUser = true;
            }
        }

        Optional<PendingUserConfig> pendingConfig = pendingUserConfigRepository.findByGoogleUserId(googleUserId);
        boolean hasPendingConfig = pendingConfig.isPresent();
        boolean consentGiven = pendingConfig.map(PendingUserConfig::isConsentGiven).orElse(true);
        boolean profileComplete = pendingConfig.map(PendingUserConfig::isProfileComplete).orElse(false);

        String requiresAction;
        if (hasPendingConfig && !consentGiven) {
            requiresAction = "give_consent";
        } else if (hasPendingConfig && !profileComplete) {
            requiresAction = "complete_patient_profile";
        } else {
            requiresAction = "";
        }

        String pendingUserConfigId = pendingConfig.map(config -> config.getId().toString()).orElse(null);

        generateAndReturnTokens(user, oauthToken, response, isNewUser, hasPendingConfig, pendingUserConfigId, requiresAction);
    }

    private User createSkeletonUser(String googleUserId, String email, String name) {
        Role userRole = roleRepository.findByCode(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("USER role not found"));

        User user = new User();
        user.setGoogleUserId(googleUserId);
        user.setEmail(email);
        user.setUsername(name != null ? name : "User");
        user.setRole(userRole);

        if (email != null) {
            user.setEmailBindex(encryptionService.encryptDeterministic(email.toLowerCase()));
        }
        if (name != null) {
            user.setUsernameBindex(encryptionService.encryptDeterministic(name.toLowerCase()));
        }

        user = userRepository.save(user);

        PendingUserConfig config = new PendingUserConfig();
        config.setGoogleUserId(googleUserId);
        config.setEmail(email);
        config.setName(name);
        config.setCreatedAt(OffsetDateTime.now());
        config.setConsentGiven(false);
        config.setProfileComplete(false);

        pendingUserConfigRepository.save(config);

        return user;
    }

    private void generateAndReturnTokens(User user, OAuth2AuthenticationToken oauthToken,
                                          HttpServletResponse response) throws IOException {
        generateAndReturnTokens(user, oauthToken, response, false, false, null, "");
    }

    private void generateAndReturnTokens(User user, OAuth2AuthenticationToken oauthToken,
                                          HttpServletResponse response,
                                          boolean isNewUser, boolean hasPendingConfig,
                                          String pendingUserConfigId, String requiresAction) throws IOException {
        List<String> roles = List.of(user.getRole() != null ? user.getRole().getCode() : "USER");

        String jwtAccessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), roles, pendingUserConfigId);
        String jwtRefreshToken = jwtTokenService.generateRefreshToken(user.getId());

        cookieService.setAccessTokenCookie(response, jwtAccessToken);
        cookieService.setRefreshTokenCookie(response, jwtRefreshToken);

        String accountStatus;
        if (isNewUser) {
            accountStatus = "profile_incomplete";
        } else if (hasPendingConfig && !"".equals(requiresAction)) {
            accountStatus = requiresAction.equals("give_consent") ? "consent_required" : "profile_incomplete";
        } else {
            accountStatus = "active";
        }

        Map<String, Object> userClaims = new LinkedHashMap<>();
        userClaims.put("id", user.getId().toString());
        userClaims.put("google_user_id", user.getGoogleUserId());
        userClaims.put("name", user.getUsername() != null ? user.getUsername() : "");
        userClaims.put("email", user.getEmail() != null ? user.getEmail() : "");
        userClaims.put("roles", roles);
        userClaims.put("account_status", accountStatus);
        userClaims.put("requires_action", requiresAction != null ? requiresAction : "");
        if (pendingUserConfigId != null) {
            userClaims.put("pendingUserConfigId", pendingUserConfigId);
        }

        String userJson = URLEncoder.encode(objectMapper.writeValueAsString(userClaims), StandardCharsets.UTF_8);

        String frontendRedirectUri = securityProperties.getOauth2().getFrontendRedirectUri();

        String redirectUrl = frontendRedirectUri + "?" + String.format(
                "access_token=%s&refresh_token=%s&token_type=Bearer&expires_in=%d&is_new_user=%s&account_status=%s&requires_action=%s&user=%s",
                jwtAccessToken,
                jwtRefreshToken,
                jwtTokenService.getAccessTokenExpirationMs() / 1000,
                isNewUser,
                accountStatus,
                URLEncoder.encode(requiresAction != null ? requiresAction : "", StandardCharsets.UTF_8),
                userJson
        );

        response.sendRedirect(redirectUrl);
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
}
