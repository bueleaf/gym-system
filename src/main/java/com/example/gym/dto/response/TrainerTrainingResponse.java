package com.example.gym.dto.response;

import java.time.LocalDate;

public record TrainerTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeResponse trainingType,
        Integer trainingDuration,
        String traineeName
) {
}