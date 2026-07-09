package com.example.gym.service;

import com.example.gym.dao.TrainingDao;
import com.example.gym.dto.TraineeTrainingSearchCriteria;
import com.example.gym.dto.TrainerTrainingSearchCriteria;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.util.ValidationUtility;
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
        ValidationUtility.validateTraining(training);
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

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }
}
