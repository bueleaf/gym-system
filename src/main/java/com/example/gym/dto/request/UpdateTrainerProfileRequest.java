package com.example.gym.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record UpdateTrainerProfileRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password,
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotNull(message = "Active status is required")
        Boolean active
) {
}
