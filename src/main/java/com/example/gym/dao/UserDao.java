package com.example.gym.dao;

import com.example.gym.entity.UserEntity;

import java.util.Optional;

public interface UserDao extends BaseDao<UserEntity> {
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
