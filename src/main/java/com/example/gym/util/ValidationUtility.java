package com.example.gym.util;

import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;

public class ValidationUtility {
    private ValidationUtility() {}

    public static void validateUser(UserEntity user) {
        requireText(user.getFirstName(), "First name");
        requireText(user.getLastName(), "Last name");
    }

    public static void validateTrainer(TrainerEntity trainer) {
        validateUser(trainer);
        requireNonNull(trainer.getSpecialization(), "Specialization");
    }

    public static void validateTraining(TrainingEntity training) {
        requireNonNull(training.getTrainee(), "Trainee");
        requireNonNull(training.getTrainer(), "Trainer");
        requireText(training.getTrainingName(), "Training name");
        requireNonNull(training.getTrainingType(), "Training type");
        requireNonNull(training.getTrainingDate(), "Training date");
        requirePositive(training.getTrainingDuration(), "Training duration");
    }

    public static void validateDateRange(
            LocalDate fromDate,
            LocalDate toDate) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "From date cannot be after to date"
            );
        }
    }

    public static void validateTrainerUsernames(
            List<String> trainerUsernames) {

        requireNonNull(trainerUsernames, "Trainer usernames");

        for (String username : trainerUsernames) {
            requireText(username, "Trainer username");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private static void requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}