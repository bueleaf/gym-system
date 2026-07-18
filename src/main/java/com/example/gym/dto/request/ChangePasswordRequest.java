package com.example.gym.dto.request;

import javax.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Old password is required")
        String oldPassword,
        @NotBlank(message = "New password is required")
        String newPassword
) {
}
