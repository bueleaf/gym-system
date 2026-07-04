package com.example.gym.daos;

import com.example.gym.dtos.TraineeTrainingSearchCriteria;
import com.example.gym.dtos.TrainerTrainingSearchCriteria;
import com.example.gym.entities.TrainingEntity;
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
                criteria.getTrainingTypeId(),
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
            Long trainingTypeId,
            SearchMode mode) {

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

        if (counterpartName != null && !counterpartName.isBlank()) {
            jpql.append(" AND (")
                    .append(counterpartAlias).append(".firstName LIKE :counterpartName")
                    .append(" OR ")
                    .append(counterpartAlias).append(".lastName LIKE :counterpartName")
                    .append(")");
        }

        if (mode == SearchMode.TRAINEE && trainingTypeId != null) {
            jpql.append(" AND tt.id = :trainingTypeId");
        }

        TypedQuery<TrainingEntity> query = entityManager.createQuery(jpql.toString(), TrainingEntity.class);
        query.setParameter(principalParam, principalUsername);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);

        if (counterpartName != null && !counterpartName.isBlank()) {
            query.setParameter("counterpartName", "%" + counterpartName + "%");
        }

        if (mode == SearchMode.TRAINEE && trainingTypeId != null) {
            query.setParameter("trainingTypeId", trainingTypeId);
        }

        return query.getResultList();
    }
}
