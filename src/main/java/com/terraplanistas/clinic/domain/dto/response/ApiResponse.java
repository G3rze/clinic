package com.terraplanistas.clinic.domain.dto.response;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
    int httpCode,
    String httpMessage,
    String httpType,
    String uri,
    T data,
    OffsetDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
            200,
            "OK",
            "SUCCESS",
            null,
            data,
            OffsetDateTime.now()
        );
    }

    public static <T> ApiResponse<T> created(T data, String uri) {
        return new ApiResponse<>(
            201,
            "Created",
            "SUCCESS",
            uri,
            data,
            OffsetDateTime.now()
        );
    }

    public static <T> ApiResponse<T> error(int httpCode, String httpMessage, String uri) {
        return new ApiResponse<>(
            httpCode,
            httpMessage,
            "ERROR",
            uri,
            null,
            OffsetDateTime.now()
        );
    }
}