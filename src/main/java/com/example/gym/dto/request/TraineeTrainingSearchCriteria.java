package com.example.gym.dto.request;

import java.time.LocalDate;

public class TraineeTrainingSearchCriteria {
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final String trainerName;
    private final String trainingTypeName;

    public TraineeTrainingSearchCriteria(
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingTypeName) {

        this.fromDate = fromDate;
        this.toDate = toDate;
        this.trainerName = trainerName;
        this.trainingTypeName = trainingTypeName;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }
}
