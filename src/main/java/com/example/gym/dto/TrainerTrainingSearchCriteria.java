package com.example.gym.dtos;

import java.time.LocalDate;

public class TrainerTrainingSearchCriteria {

    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final String traineeName;

    public TrainerTrainingSearchCriteria(
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName) {

        this.fromDate = fromDate;
        this.toDate = toDate;
        this.traineeName = traineeName;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public String getTraineeName() {
        return traineeName;
    }
}