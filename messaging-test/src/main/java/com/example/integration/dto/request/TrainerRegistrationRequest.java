package com.example.integration.dto.request;

public record TrainerRegistrationRequest(
        String firstName,
        String lastName,
        String trainingTypeName
) {
}
