package com.example.gym.application;

import com.example.gym.actuator.metric.MetricsWrapper;
import com.example.gym.dto.request.TraineeRegistrationRequest;
import com.example.gym.dto.request.UpdateTraineeProfileRequest;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.service.TraineeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraineeManagementService {
    private final TraineeService traineeService;
    private final MetricsWrapper metricsWrapper;

    public TraineeManagementService(
            TraineeService traineeService,
            MetricsWrapper metricsWrapper
    ) {
        this.traineeService = traineeService;
        this.metricsWrapper = metricsWrapper;
    }

    public CredentialsResponse register(
            TraineeRegistrationRequest request
    ) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        CredentialsResponse response =
                traineeService.createTrainee(trainee);

        metricsWrapper.recordTraineeRegistered();

        return response;
    }

    public TraineeEntity getProfile(String username) {
        return traineeService
                .getTraineeProfileByUsername(username);
    }

    public TraineeEntity updateProfile(
            String username,
            UpdateTraineeProfileRequest request
    ) {
        traineeService.updateOwnProfile(
                username,
                request
        );

        return traineeService
                .getTraineeProfileByUsername(username);
    }

    public void deleteProfile(String username) {
        traineeService.deleteTraineeByUsername(username);
    }

    public void changeActiveStatus(
            String username,
            boolean active
    ) {
        if (active) {
            traineeService.activateTrainee(username);
        } else {
            traineeService.deactivateTrainee(username);
        }
    }

    public List<TrainerEntity> getUnassignedTrainers(
            String username
    ) {
        return traineeService
                .getUnassignedTrainers(username);
    }

    public List<TrainerEntity> updateTrainers(
            String username,
            List<String> trainers
    ) {
        return traineeService
                .updateTraineeTrainers(
                        username,
                        trainers
                );
    }
}