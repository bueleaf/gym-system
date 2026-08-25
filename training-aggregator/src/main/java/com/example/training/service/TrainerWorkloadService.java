package com.example.training.service;

import com.example.training.dao.TrainerMonthlySummaryDao;
import com.example.training.dto.request.TrainerWorkloadEvent;
import com.example.training.dto.response.TrainerMonthlyWorkloadResponse;
import com.example.training.entity.TrainerMonthlySummaryEntity;
import com.example.training.exception.InvalidWorkloadException;
import com.example.training.model.ActionType;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainerWorkloadService
{
    private static final Logger LOG =
            LoggerFactory.getLogger(TrainerWorkloadService.class);

    private final TrainerMonthlySummaryDao trainerMonthlySummaryDao;

    public TrainerWorkloadService(
            TrainerMonthlySummaryDao trainerMonthlySummaryDao
    )
    {
        this.trainerMonthlySummaryDao = trainerMonthlySummaryDao;
    }

    @Transactional
    public void updateWorkload(
            TrainerWorkloadEvent trainerWorkloadEvent
    )
    {
        Integer year = trainerWorkloadEvent.trainingDate().getYear();
        Integer month = trainerWorkloadEvent.trainingDate().getMonthValue();

        TrainerMonthlySummaryEntity entity =
                trainerMonthlySummaryDao.findByUsernameAndMonthAndYear
                (
                        trainerWorkloadEvent.username(),
                        month,
                        year
                ).orElse(null);

        if (entity != null)
        {
            updateExistingWorkload(entity, trainerWorkloadEvent);
        }
        else
        {
            createWorkloadOrThrow(trainerWorkloadEvent);
        }
    }

    public TrainerMonthlyWorkloadResponse getMonthlyWorkload(
            String username,
            Integer month,
            Integer year
    )
    {
        TrainerMonthlySummaryEntity entity =
                trainerMonthlySummaryDao
                .findByUsernameAndMonthAndYear(username, month, year)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No such training summary exists"));

        return new TrainerMonthlyWorkloadResponse(
                entity.getUsername(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getIsActive(),
                entity.getYear(),
                entity.getMonth(),
                entity.getTrainingDurationTotal()
        );
    }

    private void updateExistingWorkload(
            TrainerMonthlySummaryEntity entity,
            TrainerWorkloadEvent request
    )
    {
        int difference = entity.getTrainingDurationTotal()
                - request.trainingDuration();

        if (request.actionType() == ActionType.ADD)
        {
            entity.setTrainingDurationTotal(
                    request.trainingDuration()
                            + entity.getTrainingDurationTotal());
            entity.setFirstName(request.firstName());
            entity.setLastName(request.lastName());
            entity.setIsActive(request.isActive());

            LOG.info("{} workload: trainer={} duration={} durationTotal={} year={} month={}",
                    request.actionType(),
                    entity.getUsername(),
                    request.trainingDuration(),
                    entity.getTrainingDurationTotal(),
                    entity.getYear(),
                    entity.getMonth());
        }
        else if (request.actionType() == ActionType.DELETE
                && difference >= 0)
        {
            entity.setTrainingDurationTotal(difference);
            entity.setFirstName(request.firstName());
            entity.setLastName(request.lastName());
            entity.setIsActive(request.isActive());

            LOG.info("{} workload: trainer={} duration={} durationTotal={} year={} month={}",
                    request.actionType(),
                    entity.getUsername(),
                    request.trainingDuration(),
                    entity.getTrainingDurationTotal(),
                    entity.getYear(),
                    entity.getMonth());
        }
        else
        {
            throw new InvalidWorkloadException(
                    "Cannot subtract " + request.trainingDuration()
                            + " minutes from total workload of "
                            + entity.getTrainingDurationTotal());
        }
    }

    private void createWorkloadOrThrow(
            TrainerWorkloadEvent request
    )
    {
        if (request.actionType() == ActionType.ADD)
        {
            TrainerMonthlySummaryEntity entity =
                    new TrainerMonthlySummaryEntity();
            entity.setUsername(request.username());
            entity.setFirstName(request.firstName());
            entity.setLastName(request.lastName());
            entity.setIsActive(request.isActive());
            entity.setYear(request.trainingDate().getYear());
            entity.setMonth(request.trainingDate().getMonthValue());
            entity.setTrainingDurationTotal(request.trainingDuration());

            trainerMonthlySummaryDao.save(entity);

            LOG.info("{} workload: trainer={} duration={} remainingTotal={} year={} month={}",
                    request.actionType(),
                    entity.getUsername(),
                    request.trainingDuration(),
                    entity.getTrainingDurationTotal(),
                    entity.getYear(),
                    entity.getMonth());
        }
        else
        {
            throw new EntityNotFoundException("Object not found for removal");
        }
    }

}
