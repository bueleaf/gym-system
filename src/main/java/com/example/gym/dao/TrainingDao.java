package com.example.gym.dao;

import com.example.gym.dto.TraineeTrainingSearchCriteria;
import com.example.gym.dto.TrainerTrainingSearchCriteria;
import com.example.gym.entity.TrainingEntity;
import java.util.List;

public interface TrainingDao extends BaseDao<TrainingEntity> {
    List<TrainingEntity> findTraineeTrainingsByCriteria(
            String traineeUsername,
            TraineeTrainingSearchCriteria criteria);

    List<TrainingEntity> findTrainerTrainingsByCriteria(
            String trainerUsername,
            TrainerTrainingSearchCriteria criteria);
}
