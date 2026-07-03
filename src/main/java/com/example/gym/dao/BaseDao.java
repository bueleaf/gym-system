package com.example.gym.daos;

import java.util.List;
import java.util.Optional;

public interface BaseDao<T> {
    void create(T entity);

    T update(T entity);

    Optional<T> findById(Long id);

    List<T> findAll();

    void delete(Long id);

    boolean existsById(Long id);
}