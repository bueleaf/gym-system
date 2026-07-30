package com.example.gym.application;

import com.example.gym.actuator.metric.MetricsWrapper;
import com.example.gym.dto.request.TrainerRegistrationRequest;
import com.example.gym.dto.request.UpdateTrainerProfileRequest;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.service.TrainerService;
import com.example.gym.service.TrainingTypeService;
import org.springframework.stereotype.Service;

@Service
public class TrainerManagementService {
    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService;
    private final MetricsWrapper metricsWrapper;

    public TrainerManagementService(
            TrainerService trainerService,
            TrainingTypeService trainingTypeService,
            MetricsWrapper metricsWrapper
    ) {
        this.trainerService = trainerService;
        this.trainingTypeService = trainingTypeService;
        this.metricsWrapper = metricsWrapper;
    }

    public CredentialsResponse register(
            TrainerRegistrationRequest request) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setSpecialization(trainingTypeService
                .getTrainingTypeByName(request.trainingTypeName()));

        CredentialsResponse response =
                trainerService.createTrainer(trainer);

        metricsWrapper.recordTrainerRegistered();

        return response;
    }

    public TrainerEntity getProfile(String username) {
        return trainerService.getTrainerProfileByUsername(username);
    }

    public TrainerEntity updateProfile(
            String username,
            UpdateTrainerProfileRequest request) {
        trainerService.updateOwnProfile(username, request);
        return trainerService.getTrainerProfileByUsername(username);
    }

    public void changeActiveStatus(String username,
                                   boolean active) {
        if (active) trainerService.activateTrainer(username);
        else trainerService.deactivateTrainer(username);
    }
}
