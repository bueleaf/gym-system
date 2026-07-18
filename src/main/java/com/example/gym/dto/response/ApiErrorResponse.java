package com.example.gym.dto.response;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}
