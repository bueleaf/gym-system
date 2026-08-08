package com.example.training.dto.response;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        HttpStatus status,
        String message,
        String path
) {
}
