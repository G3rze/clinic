package com.terraplanistas.clinic.http.exceptions;

public class ExternalApiException extends RuntimeException {

    private final String provider;
    private final int statusCode;
    private final String errorCode;
    private final String errorMessage;

    public ExternalApiException(String provider, String message) {
        super(message);
        this.provider = provider;
        this.statusCode = 500;
        this.errorCode = "INTERNAL_ERROR";
        this.errorMessage = message;
    }

    public ExternalApiException(String provider, String message, int statusCode) {
        super(message);
        this.provider = provider;
        this.statusCode = statusCode;
        this.errorCode = "HTTP_ERROR";
        this.errorMessage = message;
    }

    public ExternalApiException(String provider, String errorMessage, int statusCode, String errorCode) {
        super(errorMessage);
        this.provider = provider;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ExternalApiException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.statusCode = 500;
        this.errorCode = "INTERNAL_ERROR";
        this.errorMessage = message;
    }

    public String getProvider() {
        return provider;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
