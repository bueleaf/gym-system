package com.example.gym.dto.response;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        Long trainingId,
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeResponse trainingType,
        Integer trainingDuration,
        String trainerName
) {
}