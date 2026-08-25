package com.example.gym.application;

import com.example.gym.actuator.metric.MetricsWrapper;
import com.example.gym.dto.request.AddTrainingRequest;
import com.example.gym.dto.TraineeTrainingSearchCriteria;
import com.example.gym.dto.TrainerTrainingSearchCriteria;
import com.example.gym.dto.request.TrainerWorkloadEvent;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.model.ActionType;
import com.example.gym.producer.TrainerWorkloadProducer;
import com.example.gym.service.TraineeService;
import com.example.gym.service.TrainerService;
import com.example.gym.service.TrainingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingManagementService {
    private final TrainingService trainingService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final MetricsWrapper metricsWrapper;
    private final TrainerWorkloadProducer producer;

    public TrainingManagementService(
            TrainingService trainingService,
            TraineeService traineeService,
            TrainerService trainerService,
            MetricsWrapper metricsWrapper,
            TrainerWorkloadProducer producer
            ) {
        this.trainingService = trainingService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.metricsWrapper = metricsWrapper;
        this.producer = producer;
    }

    @Transactional
    public void addTraining(
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

        TrainerWorkloadEvent workloadRequest = new TrainerWorkloadEvent(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                created.getTrainingDate(),
                created.getTrainingDuration(),
                ActionType.ADD
        );

        producer.send(workloadRequest);
    }

    @Transactional
    public void deleteTraining(
            String authenticatedTrainerUsername,
            Long trainingId
    )
    {
        TrainingEntity training = trainingService.getTraining(trainingId);

        TrainerEntity trainer =
                trainerService.getTrainerByUsername(
                        authenticatedTrainerUsername
                );

        if (!authenticatedTrainerUsername.equals(training.getTrainer().getUsername()))
        {
            throw new SecurityException("Training doesn't belong to trainer");
        }

        trainingService.deleteTraining(trainingId);

        TrainerWorkloadEvent workloadEvent = new TrainerWorkloadEvent(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                ActionType.DELETE
        );

        producer.send(workloadEvent);
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
