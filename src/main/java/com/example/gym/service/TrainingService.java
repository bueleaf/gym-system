package com.example.gym.services;

import com.example.gym.daos.TrainingDao;
import com.example.gym.dtos.TraineeTrainingSearchCriteria;
import com.example.gym.dtos.TrainerTrainingSearchCriteria;
import com.example.gym.entities.TrainingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;

    @Transactional
    public TrainingEntity createTraining(TrainingEntity training) {
        logger.info("Creating new training: {}", training.getTrainingName());
        validateTraining(training);
        trainingDao.create(training);
        return training;
    }

    public TrainingEntity getTraining(Long id) {
        logger.debug("Getting training with ID: {}", id);
        return trainingDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training not found with ID: " + id));
    }

    public List<TrainingEntity> getAllTrainings() {
        logger.debug("Getting all trainings");
        return trainingDao.findAll();
    }

    public List<TrainingEntity> getTraineeTrainingsByCriteria(
            String traineeUsername,
            TraineeTrainingSearchCriteria criteria) {

        logger.debug("Getting trainee trainings for {}", traineeUsername);

        return trainingDao.findTraineeTrainingsByCriteria(
                traineeUsername,
                criteria);
    }

    public List<TrainingEntity> getTrainerTrainingsByCriteria(
            String trainerUsername,
            TrainerTrainingSearchCriteria criteria) {

        logger.debug("Getting trainer trainings for {}", trainerUsername);

        return trainingDao.findTrainerTrainingsByCriteria(
                trainerUsername,
                criteria);
    }

    private void validateTraining(TrainingEntity training) {
        if (training.getTrainee() == null) {
            throw new IllegalArgumentException("Trainee is required");
        }
        if (training.getTrainer() == null) {
            throw new IllegalArgumentException("Trainer is required");
        }
        if (training.getTrainingName() == null || training.getTrainingName().trim().isEmpty()) {
            throw new IllegalArgumentException("Training name is required");
        }
        if (training.getTrainingType() == null) {
            throw new IllegalArgumentException("Training type is required");
        }
        if (training.getTrainingDate() == null) {
            throw new IllegalArgumentException("Training date is required");
        }
        if (training.getTrainingDuration() == null || training.getTrainingDuration() <= 0) {
            throw new IllegalArgumentException("Training duration must be positive");
        }
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }
}
