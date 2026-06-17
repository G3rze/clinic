package com.terraplanistas.clinic.http.security;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailDomainValidator {

    private final SecurityProperties securityProperties;

    public EmailDomainValidator(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public boolean isEmployeeEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String employeeDomain = securityProperties.getEmployee().getEmailDomain();
        if (employeeDomain == null || employeeDomain.isBlank()) {
            return false;
        }
        return email.toLowerCase().endsWith("@" + employeeDomain.toLowerCase());
    }

    public boolean isAllowedPatientEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        List<String> allowedDomains = securityProperties.getPatient().getAllowedDomains();
        if (allowedDomains == null || allowedDomains.isEmpty()) {
            return false;
        }
        String emailLower = email.toLowerCase();
        return allowedDomains.stream()
                .map(String::toLowerCase)
                .anyMatch(domain -> emailLower.endsWith("@" + domain));
    }

    public boolean isEmailDomainAllowed(String email) {
        return isEmployeeEmail(email) || isAllowedPatientEmail(email);
    }

    public EmailUserType determineUserType(String email) {
        if (isEmployeeEmail(email)) {
            return EmailUserType.EMPLOYEE;
        }
        if (isAllowedPatientEmail(email)) {
            return EmailUserType.PATIENT;
        }
        return EmailUserType.NOT_ALLOWED;
    }

    public enum EmailUserType {
        EMPLOYEE,
        PATIENT,
        NOT_ALLOWED
    }
}
