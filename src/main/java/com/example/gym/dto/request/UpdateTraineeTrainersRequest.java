package com.example.gym.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotBlank(message = "Trainee username is required")
        String traineeUsername,
        @NotBlank(message = "Password is required")
        String password,
        @NotNull(message = "Trainer usernames are required")
        List<@NotBlank(message = "Trainer username is required")
                String> trainerUsernames
) {
}
