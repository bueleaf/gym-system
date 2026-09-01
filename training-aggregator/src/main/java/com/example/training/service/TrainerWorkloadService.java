package com.example.training.service;

import com.example.training.document.MonthSummary;
import com.example.training.document.YearSummary;
import com.example.training.dto.request.TrainerWorkloadEvent;
import com.example.training.dto.response.TrainerMonthlyWorkloadResponse;
import com.example.training.document.TrainerMonthlySummaryDocument;
import com.example.training.exception.DocumentNotFoundException;
import com.example.training.exception.InvalidWorkloadException;
import com.example.training.model.ActionType;
import com.example.training.repository.TrainerMonthlySummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrainerWorkloadService
{
    private static final Logger LOG =
            LoggerFactory.getLogger(TrainerWorkloadService.class);

    private final TrainerMonthlySummaryRepository trainerMonthlySummaryRepository;

    public TrainerWorkloadService(
            TrainerMonthlySummaryRepository trainerMonthlySummaryRepository
    )
    {
        this.trainerMonthlySummaryRepository = trainerMonthlySummaryRepository;
    }

    @Transactional
    public void updateWorkload(
            TrainerWorkloadEvent trainerWorkloadEvent
    )
    {
        TrainerMonthlySummaryDocument document =
                trainerMonthlySummaryRepository.findByUsername
                (
                        trainerWorkloadEvent.username()
                ).orElse(null);

        if (document != null)
        {
            document = updateExistingWorkload(document, trainerWorkloadEvent);
        }
        else
        {
            document = createWorkloadOrThrow(trainerWorkloadEvent);
        }

        trainerMonthlySummaryRepository.save(document);
    }

    public TrainerMonthlyWorkloadResponse getMonthlyWorkload(
            String username,
            Integer month,
            Integer year
    )
    {
        TrainerMonthlySummaryDocument document =
                trainerMonthlySummaryRepository
                .findByUsername(username)
                .orElseThrow(() -> new DocumentNotFoundException(
                        "No such training summary exists"));

        YearSummary ys = findYearSummaryInDocument(year, document);
        MonthSummary ms = findMonthSummaryInDocument(month, ys);

        return new TrainerMonthlyWorkloadResponse(
                document.getUsername(),
                document.getFirstName(),
                document.getLastName(),
                document.getIsActive(),
                ys.getYear(),
                ms.getMonth(),
                ms.getTrainingDurationTotal()
        );
    }

    private TrainerMonthlySummaryDocument updateExistingWorkload(
            TrainerMonthlySummaryDocument document,
            TrainerWorkloadEvent request
    )
    {
        Integer year = request.trainingDate().getYear();
        Integer month = request.trainingDate().getMonthValue();

        if (request.actionType() == ActionType.ADD)
        {
            Boolean isYearCreated = findOrCreateYearSummary(
                    request.trainingDuration(),
                    year,
                    month,
                    document
            );

            Boolean isMonthCreated;

            if (!isYearCreated)
            {
                isMonthCreated = findOrCreateMonthSummary(
                        request.trainingDuration(),
                        year,
                        month,
                        document
                );
            }
            else
            {
                isMonthCreated = true;
            }

            YearSummary ys = findYearSummaryInDocument(year, document);
            MonthSummary ms = findMonthSummaryInDocument(month, ys);

            if (!isMonthCreated)
            {
                ms.setTrainingDurationTotal(
                        request.trainingDuration()
                                + ms.getTrainingDurationTotal());
            }

            document.setFirstName(request.firstName());
            document.setLastName(request.lastName());
            document.setIsActive(request.isActive());

            LOG.info("{} workload: trainer={} duration={} durationTotal={} year={} month={}",
                    request.actionType(),
                    document.getUsername(),
                    request.trainingDuration(),
                    ms.getTrainingDurationTotal(),
                    year,
                    month);
        }
        else if (request.actionType() == ActionType.DELETE)
        {
            YearSummary ys = findYearSummaryInDocument(year, document);
            MonthSummary ms = findMonthSummaryInDocument(month, ys);

            int difference = ms.getTrainingDurationTotal()
                    - request.trainingDuration();

            if (difference < 0)
            {
                throw new InvalidWorkloadException(
                        "Cannot subtract " + request.trainingDuration()
                                + " minutes from total workload of "
                                + ms.getTrainingDurationTotal());
            }

            ms.setTrainingDurationTotal(difference);

            document.setFirstName(request.firstName());
            document.setLastName(request.lastName());
            document.setIsActive(request.isActive());

            LOG.info("{} workload: trainer={} duration={} durationTotal={} year={} month={}",
                    request.actionType(),
                    document.getUsername(),
                    request.trainingDuration(),
                    ms.getTrainingDurationTotal(),
                    year,
                    month);
        }

        return document;
    }

    private TrainerMonthlySummaryDocument createWorkloadOrThrow(
            TrainerWorkloadEvent request
    )
    {
        Integer year = request.trainingDate().getYear();
        Integer month = request.trainingDate().getMonthValue();

        if (request.actionType() == ActionType.ADD)
        {
            TrainerMonthlySummaryDocument document =
                    new TrainerMonthlySummaryDocument();
            document.setUsername(request.username());
            document.setFirstName(request.firstName());
            document.setLastName(request.lastName());
            document.setIsActive(request.isActive());
            document.setYears(null);

            findOrCreateYearSummary(
                    request.trainingDuration(),
                    year,
                    month,
                    document
            );

            YearSummary ys = findYearSummaryInDocument(year, document);
            MonthSummary ms = findMonthSummaryInDocument(month, ys);

            LOG.info("{} workload: trainer={} duration={} remainingTotal={} year={} month={}",
                    request.actionType(),
                    document.getUsername(),
                    request.trainingDuration(),
                    ms.getTrainingDurationTotal(),
                    ys.getYear(),
                    ms.getMonth());

            return document;
        }
        else
        {
            throw new DocumentNotFoundException("Document not found for removal");
        }
    }

    private MonthSummary findMonthSummaryInDocument(
            Integer month,
            YearSummary yearSummary
    )
    {
        if (month == null || yearSummary.getMonths() == null)
        {
            throw new DocumentNotFoundException(
                    "No training in month "
                            + month
                            + " of year "
                            + yearSummary.getYear()
                            + " exists"
            );
        }

        return yearSummary.getMonths().stream()
                .filter(e -> e.getMonth().equals(month))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException(
                        "No training in month "
                                + month
                                + " of year "
                                + yearSummary.getYear()
                                + " exists"
                ));
    }

    private YearSummary findYearSummaryInDocument(
            Integer year,
            TrainerMonthlySummaryDocument document
    )
    {
        if (year == null || document.getYears() == null)
        {
            throw new DocumentNotFoundException(
                    "No training in year " + year + " exists"
            );
        }

        return document.getYears().stream()
                .filter(e -> e.getYear().equals(year))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException(
                        "No training in year " + year + " exists"
                ));
    }

    private Boolean findOrCreateYearSummary(
            Integer trainingDuration,
            Integer year,
            Integer month,
            TrainerMonthlySummaryDocument document
    )
    {
        try
        {
            findYearSummaryInDocument(year, document);
        }
        catch (DocumentNotFoundException e)
        {
            createOrAddYears(trainingDuration, year, month, document);
            return true;
        }

        return false;
    }

    private void createOrAddYears(
            Integer trainingDuration,
            Integer year,
            Integer month,
            TrainerMonthlySummaryDocument document
    ){
        YearSummary ys = new YearSummary();
        ys.setYear(year);

        if (document.getYears() == null)
        {
            List<YearSummary> years = new ArrayList<>();
            years.add(ys);
            document.setYears(years);
        }
        else
        {
            document.getYears().add(ys);
        }

        findOrCreateMonthSummary(trainingDuration, year, month, document);
    }

    private Boolean findOrCreateMonthSummary(
            Integer trainingDuration,
            Integer year,
            Integer month,
            TrainerMonthlySummaryDocument document
    )
    {
        YearSummary ys = findYearSummaryInDocument(year, document);

        try
        {
            findMonthSummaryInDocument(month, ys);
        }
        catch (DocumentNotFoundException e)
        {
            createOrAddMonths(trainingDuration, ys, month);
            return true;
        }

        return false;
    }

    private void createOrAddMonths(
            Integer trainingDuration,
            YearSummary ys,
            Integer month
    ){
        MonthSummary ms = new MonthSummary();
        ms.setMonth(month);
        ms.setTrainingDurationTotal(trainingDuration);

        if (ys.getMonths() == null)
        {
            List<MonthSummary> months = new ArrayList<>();
            months.add(ms);
            ys.setMonths(months);
        }
        else
        {
            ys.getMonths().add(ms);
        }
    }

}
