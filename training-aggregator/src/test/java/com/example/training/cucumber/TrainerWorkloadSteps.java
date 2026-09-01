package com.example.training.cucumber;

import com.example.training.document.MonthSummary;
import com.example.training.document.TrainerMonthlySummaryDocument;
import com.example.training.document.YearSummary;
import com.example.training.dto.request.TrainerWorkloadEvent;
import com.example.training.model.ActionType;
import com.example.training.repository.TrainerMonthlySummaryRepository;
import com.example.training.service.TrainerWorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TrainerWorkloadSteps
{
    private final TrainerWorkloadService service;
    private final TrainerMonthlySummaryRepository repository;

    private String username;
    private TrainerWorkloadEvent event;
    private long repositoryCountBefore;

    private static final Integer MONTH = 9;
    private static final Integer YEAR = 2026;

    public TrainerWorkloadSteps(TrainerWorkloadService service,
                                TrainerMonthlySummaryRepository repository)
    {
        this.service = service;
        this.repository = repository;
    }

    @Before
    public void cleanBeforeEach()
    {
        repository.deleteAll();
    }

    @Given("no workload exists for trainer {string}")
    public void noWorkloadExists(String username)
    {
        this.username = username;

        repository.findByUsername(username)
                .ifPresent(repository::delete);
    }

    @When("ADD workload event of {int} minutes is processed")
    public void addEventOfMinutes(int minutes)
    {
        TrainerWorkloadEvent event = new TrainerWorkloadEvent(
                username,
                "RandomName",
                "RandomSurname",
                true,
                LocalDate.of(YEAR, MONTH, 10),
                minutes,
                ActionType.ADD
        );

        service.updateWorkload(event);
    }

    @Then("trainer workload should contain {int} minutes")
    public void checkMinutes(int expectedMinutes)
    {
        TrainerMonthlySummaryDocument document =
                repository.findByUsername(username)
                .orElseThrow();

        YearSummary ys = document.getYears().stream()
                .filter(e -> e.getYear().equals(YEAR))
                .findFirst()
                .orElseThrow();

        MonthSummary ms = ys.getMonths().stream()
                .filter(e -> e.getMonth().equals(MONTH))
                .findFirst().orElseThrow();

        assertEquals(expectedMinutes, ms.getTrainingDurationTotal());
    }

    @Given("workload event without trainer username")
    public void workloadEventWithoutUsername()
    {
        repositoryCountBefore = repository.count();

        event = new TrainerWorkloadEvent(
                "",
                "RandomName",
                "RandomSurname",
                true,
                LocalDate.of(YEAR, MONTH, 10),
                60,
                ActionType.ADD
        );
    }

    @When("workload event is processed")
    public void workloadEventIsProcessed()
    {
        assertThrows(ConstraintViolationException.class,
                () -> service.updateWorkload(event));
    }

    @Then("no trainer workload is created")
    public void noTrainerWorkloadIsCreated()
    {
        assertEquals(repositoryCountBefore, repository.count());
    }
}
