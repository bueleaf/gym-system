package com.example.gym.dto.request;

import jakarta.validation.constraints.NotNull;

public record ActivationRequest(
        @NotNull(message = "Active status is required")
        Boolean active
) {
}