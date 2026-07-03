package com.example.gym.daos;

import com.example.gym.entities.TrainerEntity;
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
    public List<TrainerEntity> findBySpecialization(String specialization) {
        logger.debug("Finding trainers by specialization: {}", specialization);
        String jpql = "SELECT t FROM TrainerEntity t JOIN t.specialization s WHERE s.trainingTypeName = :specialization";
        TypedQuery<TrainerEntity> query = entityManager.createQuery(jpql, TrainerEntity.class);
        query.setParameter("specialization", specialization);
        return query.getResultList();
    }

    @Override
    public List<TrainerEntity> findByFirstNameAndLastName(String firstName, String lastName) {
        logger.debug("Finding trainers by name: {} {}", firstName, lastName);
        String jpql = "SELECT t FROM TrainerEntity t WHERE t.firstName = :firstName AND t.lastName = :lastName";
        TypedQuery<TrainerEntity> query = entityManager.createQuery(jpql, TrainerEntity.class);
        query.setParameter("firstName", firstName);
        query.setParameter("lastName", lastName);
        return query.getResultList();
    }
}