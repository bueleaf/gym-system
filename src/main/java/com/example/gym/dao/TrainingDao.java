package com.example.gym.daos;

import com.example.gym.dtos.TraineeTrainingSearchCriteria;
import com.example.gym.dtos.TrainerTrainingSearchCriteria;
import com.example.gym.entities.TrainingEntity;
import java.util.List;

public interface TrainingDao extends BaseDao<TrainingEntity> {
    List<TrainingEntity> findTraineeTrainingsByCriteria(
            String traineeUsername,
            TraineeTrainingSearchCriteria criteria);

    List<TrainingEntity> findTrainerTrainingsByCriteria(
            String trainerUsername,
            TrainerTrainingSearchCriteria criteria);
}
