package com.example.gym.application;

import com.example.gym.actuator.metric.MetricsWrapper;
import com.example.gym.dto.request.TrainerRegistrationRequest;
import com.example.gym.dto.request.UpdateTrainerProfileRequest;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.service.AuthenticationService;
import com.example.gym.service.TrainerService;
import com.example.gym.service.TrainingTypeService;
import org.springframework.stereotype.Service;

@Service
public class TrainerManagementService {
    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService;
    private final AuthenticationService authenticationService;
    private final MetricsWrapper metricsWrapper;

    public TrainerManagementService(TrainerService trainerService, TrainingTypeService trainingTypeService, AuthenticationService authenticationService, MetricsWrapper metricsWrapper) {
        this.trainerService = trainerService;
        this.trainingTypeService = trainingTypeService;
        this.authenticationService = authenticationService;
        this.metricsWrapper = metricsWrapper;
    }

    public TrainerEntity register(TrainerRegistrationRequest request) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setSpecialization(trainingTypeService
                .getTrainingTypeByName(request.trainingTypeName()));

        TrainerEntity created = trainerService.createTrainer(trainer);

        metricsWrapper.recordTrainerRegistered();

        return created;
    }

    public TrainerEntity getProfile(String username, String password) {
        authenticate(username, password);

        return trainerService.getTrainerProfileByUsername(username);
    }

    public TrainerEntity updateProfile(
            String username,
            String password,
            UpdateTrainerProfileRequest request) {
        authenticate(username, password);

        trainerService.updateOwnProfile(username, request);
        return trainerService.getTrainerProfileByUsername(username);
    }

    public void changeActiveStatus(String username, String password, boolean active) {
        authenticate(username, password);

        if (active) trainerService.activateTrainer(username);
        else trainerService.deactivateTrainer(username);
    }

    private void authenticate(String username, String password) {
        if (!(authenticationService.authenticate(username, password)
                instanceof TrainerEntity)) {
            throw new SecurityException("User is not a trainer: " + username);
        }
    }
}
