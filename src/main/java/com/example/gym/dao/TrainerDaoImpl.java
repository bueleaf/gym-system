package com.example.gym.dao;

import com.example.gym.entity.TrainerEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public boolean existsByUsernameBase(String usernameBase) {

        String jpql = """
                SELECT COUNT(t)
                FROM TrainerEntity t
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
