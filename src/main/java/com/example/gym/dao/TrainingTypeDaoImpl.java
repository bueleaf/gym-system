package com.example.gym.daos;

import com.example.gym.entities.TrainingTypeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingTypeEntity> findById(Long id) {
        logger.debug("Finding TrainingType by ID: {}", id);
        return Optional.ofNullable(entityManager.find(TrainingTypeEntity.class, id));
    }

    @Override
    public Optional<TrainingTypeEntity> findByName(String name) {
        logger.debug("Finding TrainingType by name: {}", name);
        String jpql = "SELECT tt FROM TrainingTypeEntity tt WHERE tt.trainingTypeName = :name";
        TypedQuery<TrainingTypeEntity> query = entityManager.createQuery(jpql, TrainingTypeEntity.class);
        query.setParameter("name", name);
        List<TrainingTypeEntity> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<TrainingTypeEntity> findAll() {
        logger.debug("Finding all TrainingTypes");
        String jpql = "SELECT tt FROM TrainingTypeEntity tt";
        return entityManager.createQuery(jpql, TrainingTypeEntity.class).getResultList();
    }
}