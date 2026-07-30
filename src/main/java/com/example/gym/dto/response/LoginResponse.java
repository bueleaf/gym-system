package com.example.gym.dto.response;

public record LoginResponse(
        String token,
        String type
) {
}
