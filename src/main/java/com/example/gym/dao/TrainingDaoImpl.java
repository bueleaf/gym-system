package com.example.gym.dao;

import com.example.gym.dto.request.TraineeTrainingSearchCriteria;
import com.example.gym.dto.request.TrainerTrainingSearchCriteria;
import com.example.gym.entity.TrainingEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

@Repository
public class TrainingDaoImpl extends BaseDaoImpl<TrainingEntity> implements TrainingDao {
    private enum SearchMode { TRAINEE, TRAINER }

    @Override
    protected Class<TrainingEntity> getEntityClass() {
        return TrainingEntity.class;
    }

    @Override
    public List<TrainingEntity> findTraineeTrainingsByCriteria(
            String traineeUsername,
            TraineeTrainingSearchCriteria criteria) {
        return findTrainingsByCriteria(
                traineeUsername,
                criteria.getFromDate(),
                criteria.getToDate(),
                criteria.getTrainerName(),
                criteria.getTrainingTypeName(),
                SearchMode.TRAINEE
        );
    }

    @Override
    public List<TrainingEntity> findTrainerTrainingsByCriteria(
            String trainerUsername,
            TrainerTrainingSearchCriteria criteria) {
        return findTrainingsByCriteria(
                trainerUsername,
                criteria.getFromDate(),
                criteria.getToDate(),
                criteria.getTraineeName(),
                null,
                SearchMode.TRAINER
        );
    }

    private List<TrainingEntity> findTrainingsByCriteria(
            String principalUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String counterpartName,
            String trainingTypeName,
            SearchMode mode) {

        String normalizedCounterpartName = normalize(counterpartName);
        String normalizedTrainingTypeName = normalize(trainingTypeName);
        String principalAlias = mode == SearchMode.TRAINEE ? "te" : "tr";
        String counterpartAlias = mode == SearchMode.TRAINEE ? "tr" : "te";
        String principalParam = mode == SearchMode.TRAINEE ? "traineeUsername" : "trainerUsername";

        StringBuilder jpql = new StringBuilder(
                "SELECT t FROM TrainingEntity t " +
                        "JOIN FETCH t.trainee te " +
                        "JOIN FETCH t.trainer tr " +
                        "JOIN FETCH t.trainingType tt " +
                        "WHERE " + principalAlias + ".username = :" + principalParam
        );

        if (fromDate != null) jpql.append(" AND t.trainingDate >= :fromDate");
        if (toDate != null) jpql.append(" AND t.trainingDate <= :toDate");

        if (normalizedCounterpartName != null) {
            jpql.append(" AND LOWER(CONCAT(")
                    .append(counterpartAlias).append(".firstName, ' ', ")
                    .append(counterpartAlias).append(".lastName)) ")
                    .append("LIKE :counterpartName");
        }

        if (mode == SearchMode.TRAINEE
                && normalizedTrainingTypeName != null) {
            jpql.append(" AND LOWER(tt.trainingTypeName) = :trainingTypeName");
        }

        TypedQuery<TrainingEntity> query = entityManager.createQuery(jpql.toString(), TrainingEntity.class);
        query.setParameter(principalParam, principalUsername);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);

        if (normalizedCounterpartName != null) {
            query.setParameter(
                    "counterpartName",
                    "%" + normalizedCounterpartName + "%"
            );
        }

        if (mode == SearchMode.TRAINEE
                && normalizedTrainingTypeName != null) {
            query.setParameter(
                    "trainingTypeName",
                    normalizedTrainingTypeName
            );
        }

        return query.getResultList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }
}
