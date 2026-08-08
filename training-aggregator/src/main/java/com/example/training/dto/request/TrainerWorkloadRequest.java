package com.example.training.dto.request;

import com.example.training.model.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TrainerWorkloadRequest
        (
                @NotBlank(message = "Trainer username is required")
                String username,
                @NotBlank(message = "Trainer first name is required")
                String firstName,
                @NotBlank(message = "Trainer last name is required")
                String lastName,
                @NotNull(message = "Trainer active status is required")
                Boolean isActive,
                @NotNull(message = "Training date is required")
                LocalDate trainingDate,
                @NotNull(message = "Training duration is required")
                @Positive
                Integer trainingDuration,
                @NotNull(message = "Action type is required")
                ActionType actionType
        )
{
}
