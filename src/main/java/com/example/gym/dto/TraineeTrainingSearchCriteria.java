package com.example.gym.dtos;

import java.time.LocalDate;

public class TraineeTrainingSearchCriteria {

    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final String trainerName;
    private final Long trainingTypeId;

    public TraineeTrainingSearchCriteria(
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            Long trainingTypeId) {

        this.fromDate = fromDate;
        this.toDate = toDate;
        this.trainerName = trainerName;
        this.trainingTypeId = trainingTypeId;
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

    public Long getTrainingTypeId() {
        return trainingTypeId;
    }
}