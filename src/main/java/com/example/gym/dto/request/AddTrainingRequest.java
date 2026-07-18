package com.example.gym.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

public record AddTrainingRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password,
        @NotBlank(message = "Trainee username is required")
        String traineeUsername,
        @NotBlank(message = "Trainer username is required")
        String trainerUsername,
        @NotBlank(message = "Training name is required")
        String trainingName,
        @NotNull(message = "Training date is required")
        LocalDate trainingDate,
        @NotNull(message = "Training duration is required")
        @Positive(message = "Training duration must be positive")
        Integer trainingDuration
) {
}
