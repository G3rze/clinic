package com.terraplanistas.clinic.http.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private Cors cors = new Cors();
    private OAuth2 oauth2 = new OAuth2();
    private Employee employee = new Employee();
    private Patient patient = new Patient();
    private Consent consent = new Consent();
    private User user = new User();

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public OAuth2 getOauth2() {
        return oauth2;
    }

    public void setOauth2(OAuth2 oauth2) {
        this.oauth2 = oauth2;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Consent getConsent() {
        return consent;
    }

    public void setConsent(Consent consent) {
        this.consent = consent;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:3000";
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String allowedHeaders = "Authorization,Content-Type,X-Requested-With";
        private boolean allowCredentials = true;
        private long maxAge = 3600;

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public String getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(String allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public String getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(String allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }

    public static class OAuth2 {
        private String successRedirectUrl = "/";

        public String getSuccessRedirectUrl() {
            return successRedirectUrl;
        }

        public void setSuccessRedirectUrl(String successRedirectUrl) {
            this.successRedirectUrl = successRedirectUrl;
        }
    }

    public static class Employee {
        private String emailDomain;

        public String getEmailDomain() {
            return emailDomain;
        }

        public void setEmailDomain(String emailDomain) {
            this.emailDomain = emailDomain;
        }
    }

    public static class Patient {
        private List<String> allowedDomains = List.of("gmail.com");
        private boolean autoCreate = true;

        public List<String> getAllowedDomains() {
            return allowedDomains;
        }

        public void setAllowedDomains(List<String> allowedDomains) {
            this.allowedDomains = allowedDomains;
        }

        public boolean isAutoCreate() {
            return autoCreate;
        }

        public void setAutoCreate(boolean autoCreate) {
            this.autoCreate = autoCreate;
        }
    }

    public static class Consent {
        private String version = "v1.0";

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class User {
        private int anonymizationRetentionDays = 30;

        public int getAnonymizationRetentionDays() {
            return anonymizationRetentionDays;
        }

        public void setAnonymizationRetentionDays(int anonymizationRetentionDays) {
            this.anonymizationRetentionDays = anonymizationRetentionDays;
        }
    }
}