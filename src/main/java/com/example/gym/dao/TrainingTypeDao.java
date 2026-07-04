package com.example.gym.daos;

import com.example.gym.entities.TrainingTypeEntity;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {
    Optional<TrainingTypeEntity> findById(Long id);

    Optional<TrainingTypeEntity> findByName(String name);

    List<TrainingTypeEntity> findAll();
}