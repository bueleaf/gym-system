package com.example.gym.daos;

import com.example.gym.entities.TrainerEntity;
import java.util.List;
import java.util.Optional;

public interface TrainerDao extends BaseDao<TrainerEntity> {
    Optional<TrainerEntity> findByUsername(String username);

    List<TrainerEntity> findBySpecialization(String specialization);

    List<TrainerEntity> findByFirstNameAndLastName(String firstName, String lastName);
}