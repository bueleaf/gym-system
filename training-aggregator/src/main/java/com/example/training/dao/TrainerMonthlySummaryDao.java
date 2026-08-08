package com.example.training.dao;

import com.example.training.entity.TrainerMonthlySummaryEntity;

import java.util.Optional;

public interface TrainerMonthlySummaryDao
{
    void save(TrainerMonthlySummaryEntity entity);

    Optional<TrainerMonthlySummaryEntity> findByUsernameAndMonthAndYear
            (String username,
             Integer month,
             Integer year);
}
