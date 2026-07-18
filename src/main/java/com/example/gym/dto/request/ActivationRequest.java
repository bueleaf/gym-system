package com.example.gym.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

public record ActivationRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password,
        @NotNull(message = "Active status is required")
        Boolean active
) {
}
