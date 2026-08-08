package com.example.training.dao;

import com.example.training.entity.TrainerMonthlySummaryEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainerMonthlySummaryDaoImpl
        implements TrainerMonthlySummaryDao
{
    @PersistenceContext
    private EntityManager entityManager;

    public void save(TrainerMonthlySummaryEntity entity)
    {
        entityManager.persist(entity);
    }

    public Optional<TrainerMonthlySummaryEntity> findByUsernameAndMonthAndYear
            (String username,
             Integer month,
             Integer year)
    {
        String jpql = "SELECT summary " +
                "FROM TrainerMonthlySummaryEntity summary " +
                "WHERE summary.username = :username " +
                "AND summary.month = :month " +
                "AND summary.year = :year";

        TypedQuery<TrainerMonthlySummaryEntity> query =
                entityManager.createQuery(jpql, TrainerMonthlySummaryEntity.class);

        query.setParameter("username", username);
        query.setParameter("month", month);
        query.setParameter("year", year);

        return query.getResultStream().findFirst();
    }
}
