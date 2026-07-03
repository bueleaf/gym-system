package com.example.gym.daos;

import com.example.gym.entities.UserEntity;

import java.util.Optional;

public interface UserDao extends BaseDao<UserEntity> {
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}