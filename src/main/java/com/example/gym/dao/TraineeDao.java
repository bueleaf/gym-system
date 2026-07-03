package com.example.gym.daos;

import com.example.gym.entities.TraineeEntity;
import java.util.*;

public interface TraineeDao extends BaseDao<TraineeEntity> {
    Optional<TraineeEntity> findByUsername(String username);

    List<TraineeEntity> findByFirstNameAndLastName(String firstName, String lastName);
}