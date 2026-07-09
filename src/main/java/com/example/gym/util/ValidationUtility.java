package com.example.gym.util;

import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.entity.UserEntity;

public class ValidationUtility {
    private ValidationUtility() {}

    public static void validateUser(UserEntity user) {
        requireText(user.getFirstName(), "First name");
        requireText(user.getLastName(), "Last name");
    }

    public static void validateTrainee(TraineeEntity trainee) {
        validateUser(trainee);
        requireNonNull(trainee.getDateOfBirth(), "Date of birth");
        requireText(trainee.getAddress(), "Address");
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