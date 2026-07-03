package com.example.gym.dtos;

import java.time.LocalDate;

public class UpdateTraineeProfileRequest {
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String address;

    public UpdateTraineeProfileRequest(String firstName, String lastName,
                                       LocalDate dateOfBirth, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
}