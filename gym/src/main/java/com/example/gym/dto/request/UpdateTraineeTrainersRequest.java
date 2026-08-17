package com.example.gym.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotNull(message = "Trainer usernames are required")
        List<String> trainerUsernames
) {
}