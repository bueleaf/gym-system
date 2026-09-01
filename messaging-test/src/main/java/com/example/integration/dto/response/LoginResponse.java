package com.example.integration.dto.response;

public record LoginResponse(
        String token,
        String type
) {
}
