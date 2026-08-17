package com.example.gym.application;

import com.example.gym.entity.TrainingTypeEntity;
import com.example.gym.service.TrainingTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingTypeManagementService {
    private final TrainingTypeService trainingTypeService;

    public TrainingTypeManagementService(
            TrainingTypeService trainingTypeService
    ) {
        this.trainingTypeService =
                trainingTypeService;
    }

    public List<TrainingTypeEntity>
    getTrainingTypes() {
        return trainingTypeService
                .getAllTrainingTypes();
    }
}