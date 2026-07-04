package com.example.gym.dtos;

public class UpdateTrainerProfileRequest {
    private final String firstName;
    private final String lastName;
    private final Long specializationId;

    public UpdateTrainerProfileRequest(String firstName, String lastName, Long specializationId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specializationId = specializationId;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Long getSpecializationId() { return specializationId; }
}