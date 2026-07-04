package com.example.gym.daos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public abstract class BaseDaoImpl<T> implements BaseDao<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    protected EntityManager entityManager;

    protected abstract Class<T> getEntityClass();

    @Override
    public void create(T entity) {
        entityManager.persist(entity);
    }

    @Override
    public T update(T entity) {
        return entityManager.contains(entity) ? entity : entityManager.merge(entity);
    }

    @Override
    public Optional<T> findById(Long id) {
        T entity = entityManager.find(getEntityClass(), id);
        return Optional.ofNullable(entity);
    }

    @Override
    public List<T> findAll() {
        String jpql = "SELECT e FROM " + getEntityClass().getSimpleName() + " e";
        TypedQuery<T> query = entityManager.createQuery(jpql, getEntityClass());
        return query.getResultList();
    }

    @Override
    public void delete(Long id) {
        T entity = entityManager.find(getEntityClass(), id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }
}