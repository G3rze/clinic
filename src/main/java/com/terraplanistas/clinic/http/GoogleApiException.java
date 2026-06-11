package com.terraplanistas.clinic.http;

public class GoogleApiException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    public GoogleApiException(String message) {
        super(message);
        this.statusCode = 500;
        this.errorCode = "INTERNAL_ERROR";
    }

    public GoogleApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = "HTTP_ERROR";
    }

    public GoogleApiException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public GoogleApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
        this.errorCode = "INTERNAL_ERROR";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}