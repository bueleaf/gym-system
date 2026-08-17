package com.example.gym.dto.response;

public record TrainerSummaryResponse(
        String username,
        String firstName,
        String lastName,
        TrainingTypeResponse specialization
) {
}