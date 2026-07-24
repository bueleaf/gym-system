package com.example.gym.application;

import com.example.gym.actuator.metric.MetricsWrapper;
import com.example.gym.dto.request.AddTrainingRequest;
import com.example.gym.dto.request.TraineeTrainingSearchCriteria;
import com.example.gym.dto.request.TrainerTrainingSearchCriteria;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.entity.TrainingTypeEntity;
import com.example.gym.service.AuthenticationService;
import com.example.gym.service.TraineeService;
import com.example.gym.service.TrainerService;
import com.example.gym.service.TrainingService;
import com.example.gym.service.TrainingTypeService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TrainingManagementService {
    private final TrainingService trainingService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService;
    private final AuthenticationService authenticationService;
    private final MetricsWrapper metricsWrapper;

    public TrainingManagementService(
            TrainingService trainingService,
            TraineeService traineeService,
            TrainerService trainerService,
            TrainingTypeService trainingTypeService,
            AuthenticationService authenticationService,
            MetricsWrapper metricsWrapper) {
        this.trainingService = trainingService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingTypeService = trainingTypeService;
        this.authenticationService = authenticationService;
        this.metricsWrapper = metricsWrapper;
    }

    public TrainingEntity addTraining(
            String username,
            String password,
            AddTrainingRequest request) {
        authenticate(username, password);

        TraineeEntity trainee = traineeService
                .getTraineeByUsername(request.traineeUsername());
        TrainerEntity trainer = trainerService
                .getTrainerByUsername(request.trainerUsername());

        TrainingEntity training = new TrainingEntity();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(request.trainingName());
        training.setTrainingDate(request.trainingDate());
        training.setTrainingDuration(request.trainingDuration());
        training.setTrainingType(trainer.getSpecialization());

        TrainingEntity created = trainingService.createTraining(training);

        metricsWrapper.recordTrainingCreated();

        return created;
    }

    public List<TrainingEntity> getTraineeTrainings(String username, String password, TraineeTrainingSearchCriteria criteria) {
        authenticateTrainee(username, password);

        return trainingService.getTraineeTrainingsByCriteria(username, criteria);
    }

    public List<TrainingEntity> getTrainerTrainings(String username, String password, TrainerTrainingSearchCriteria criteria) {
        authenticateTrainer(username, password);

        return trainingService.getTrainerTrainingsByCriteria(username, criteria);
    }

    public List<TrainingTypeEntity> getTrainingTypes(String username, String password) {
        authenticate(username, password);

        return trainingTypeService.getAllTrainingTypes();
    }

    private void authenticate(String username, String password) {
        authenticationService.authenticate(username, password);
    }

    private void authenticateTrainee(String username, String password) {
        if (!(authenticationService.authenticate(username, password)
                instanceof TraineeEntity))
            throw new SecurityException("User is not a trainee: " + username);
    }

    private void authenticateTrainer(String username, String password) {
        if (!(authenticationService.authenticate(username, password)
                instanceof TrainerEntity))
            throw new SecurityException("User is not a trainer: " + username);
    }
}
