package com.example.gym.dao;

import com.example.gym.entity.TrainerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl extends BaseDaoImpl<TrainerEntity> implements TrainerDao {
    @Override
    protected Class<TrainerEntity> getEntityClass() {
        return TrainerEntity.class;
    }

    @Override
    public Optional<TrainerEntity> findByUsername(String username) {
        logger.debug("Finding trainer by username: {}", username);
        String jpql = "SELECT t FROM TrainerEntity t LEFT JOIN FETCH t.specialization WHERE t.username = :username";
        TypedQuery<TrainerEntity> query = entityManager.createQuery(jpql, TrainerEntity.class);
        query.setParameter("username", username);

        List<TrainerEntity> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
