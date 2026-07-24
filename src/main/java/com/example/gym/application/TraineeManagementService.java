package com.example.gym.application;

import com.example.gym.actuator.metric.MetricsWrapper;
import com.example.gym.dto.request.TraineeRegistrationRequest;
import com.example.gym.dto.request.UpdateTraineeProfileRequest;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.service.AuthenticationService;
import com.example.gym.service.TraineeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraineeManagementService {
    private final TraineeService traineeService;
    private final AuthenticationService authenticationService;
    private final MetricsWrapper metricsWrapper;

    public TraineeManagementService(
            TraineeService traineeService,
            AuthenticationService authenticationService,
            MetricsWrapper metricsWrapper) {
        this.traineeService = traineeService;
        this.authenticationService = authenticationService;
        this.metricsWrapper = metricsWrapper;
    }

    public TraineeEntity register(TraineeRegistrationRequest request) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        TraineeEntity created = traineeService.createTrainee(trainee);
        metricsWrapper.recordTraineeRegistered();
        return created;
    }

    public TraineeEntity getProfile(String username, String password) {
        authenticate(username, password);

        return traineeService.getTraineeProfileByUsername(username);
    }

    public TraineeEntity updateProfile(
            String username,
            String password,
            UpdateTraineeProfileRequest request) {
        authenticate(username, password);

        traineeService.updateOwnProfile(username, request);
        return traineeService.getTraineeProfileByUsername(username);
    }

    public void deleteProfile(String username, String password) {
        authenticate(username, password);

        traineeService.deleteTraineeByUsername(username);
    }

    public void changeActiveStatus(
            String username,
            String password,
            boolean active) {
        authenticate(username, password);

        if (active) {
            traineeService.activateTrainee(username);
        } else {
            traineeService.deactivateTrainee(username);
        }
    }

    public List<TrainerEntity> getUnassignedTrainers(
            String username,
            String password) {
        authenticate(username, password);

        return traineeService.getUnassignedTrainers(username);
    }

    public List<TrainerEntity> updateTrainers(
            String username,
            String password,
            List<String> trainers) {
        authenticate(username, password);

        return traineeService.updateTraineeTrainers(username, trainers);
    }

    private void authenticate(String username, String password) {
        if (!(authenticationService.authenticate(username, password)
                instanceof TraineeEntity)) {
            throw new SecurityException("User is not a trainee: " + username);
        }
    }
}
