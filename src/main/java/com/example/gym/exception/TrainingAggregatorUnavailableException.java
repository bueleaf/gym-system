package com.example.gym.exception;

public class TrainingAggregatorUnavailableException extends RuntimeException {
    public TrainingAggregatorUnavailableException(String message,
                                                  Throwable cause) {
        super(message, cause);
    }
}
