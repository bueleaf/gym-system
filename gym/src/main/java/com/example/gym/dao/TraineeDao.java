package com.example.gym.dao;

import com.example.gym.entity.TraineeEntity;
import java.util.*;

public interface TraineeDao extends BaseDao<TraineeEntity> {
    Optional<TraineeEntity> findByUsername(String username);
}
