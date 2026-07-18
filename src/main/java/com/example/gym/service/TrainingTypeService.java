package com.example.gym.service;

import com.example.gym.dao.TrainingTypeDao;
import com.example.gym.entity.TrainingTypeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingTypeService {

    private TrainingTypeDao trainingTypeDao;

    public List<TrainingTypeEntity> getAllTrainingTypes() {
        return trainingTypeDao.findAll();
    }

    public TrainingTypeEntity getTrainingTypeByName(String name) {
        return trainingTypeDao.findByName(name)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid training type: " + name
                        )
                );
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }
}
