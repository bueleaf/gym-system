package com.example.integration.dto.request;

import java.time.LocalDate;

public record AddTrainingRequest(
        String traineeUsername,
        String trainingName,
        LocalDate trainingDate,
        Integer trainingDuration
) {
}
