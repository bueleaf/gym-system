package com.example.gym.dao;

import com.example.gym.entity.TrainerEntity;
import java.util.List;
import java.util.Optional;

public interface TrainerDao extends BaseDao<TrainerEntity> {
    Optional<TrainerEntity> findByUsername(String username);

    boolean existsByUsernameBase(String usernameBase);
}
