package com.example.gym.application;

import com.example.gym.actuator.metric.MetricsWrapper;
import com.example.gym.dto.request.AddTrainingRequest;
import com.example.gym.dto.TraineeTrainingSearchCriteria;
import com.example.gym.dto.TrainerTrainingSearchCriteria;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.entity.TrainingTypeEntity;
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
    private final MetricsWrapper metricsWrapper;

    public TrainingManagementService(
            TrainingService trainingService,
            TraineeService traineeService,
            TrainerService trainerService,
            MetricsWrapper metricsWrapper) {
        this.trainingService = trainingService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.metricsWrapper = metricsWrapper;
    }

    public TrainingEntity addTraining(
            String authenticatedTrainerUsername,
            AddTrainingRequest request
    ) {
        TraineeEntity trainee =
                traineeService.getTraineeByUsername(
                        request.traineeUsername()
                );

        TrainerEntity trainer =
                trainerService.getTrainerByUsername(
                        authenticatedTrainerUsername
                );

        TrainingEntity training =
                new TrainingEntity();

        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(
                request.trainingName()
        );
        training.setTrainingDate(
                request.trainingDate()
        );
        training.setTrainingDuration(
                request.trainingDuration()
        );
        training.setTrainingType(
                trainer.getSpecialization()
        );

        TrainingEntity created =
                trainingService.createTraining(training);

        metricsWrapper.recordTrainingCreated();

        return created;
    }

    public List<TrainingEntity> getTraineeTrainings(
            String username,
            TraineeTrainingSearchCriteria criteria
    ) {
        return trainingService.getTraineeTrainingsByCriteria(
                username, criteria);
    }

    public List<TrainingEntity> getTrainerTrainings(
            String username,
            TrainerTrainingSearchCriteria criteria) {
        return trainingService.getTrainerTrainingsByCriteria(
                username, criteria);
    }
}
