package com.example.gym.dao;

import com.example.gym.entity.TraineeEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDaoImpl extends BaseDaoImpl<TraineeEntity> implements TraineeDao {
    @Override
    protected Class<TraineeEntity> getEntityClass() {
        return TraineeEntity.class;
    }

    @Override
    public Optional<TraineeEntity> findByUsername(String username) {
        logger.debug("Finding trainee by username: {}", username);
        String jpql = "SELECT t FROM TraineeEntity t WHERE t.username = :username";
        TypedQuery<TraineeEntity> query = entityManager.createQuery(jpql, TraineeEntity.class);
        query.setParameter("username", username);

        List<TraineeEntity> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByUsernameBase(String usernameBase) {

        String jpql = """
                SELECT COUNT(t)
                FROM TraineeEntity t
                WHERE t.username = :usernameBase
                   OR t.username LIKE :usernamePrefix
                """;

        Long count = entityManager
                .createQuery(jpql, Long.class)
                .setParameter("usernameBase", usernameBase)
                .setParameter("usernamePrefix", usernameBase + "%")
                .getSingleResult();

        return count > 0;
    }
}
