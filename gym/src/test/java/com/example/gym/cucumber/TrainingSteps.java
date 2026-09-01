package com.example.gym.cucumber;

import com.example.gym.dao.TraineeDao;
import com.example.gym.dao.TrainerDao;
import com.example.gym.dao.TrainingDao;
import com.example.gym.dao.TrainingTypeDao;
import com.example.gym.entity.*;
import com.example.gym.model.Role;
import com.example.gym.service.TrainingService;
import com.example.gym.service.UserAccountService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TrainingSteps
{
    private final TrainingService trainingService;
    private final UserAccountService userAccountService;
    private final TrainingTypeDao trainingTypeDao;
    private final TrainingDao trainingDao;
    private final TrainerDao trainerDao;
    private final TraineeDao traineeDao;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    private TrainerEntity trainer;
    private TraineeEntity trainee;
    private TrainingEntity training;

    public TrainingSteps(TrainingService trainingService,
                         UserAccountService userAccountService,
                         TrainingTypeDao trainingTypeDao,
                         TrainingDao trainingDao,
                         TrainerDao trainerDao,
                         TraineeDao traineeDao,
                         JdbcTemplate jdbcTemplate,
                         TransactionTemplate transactionTemplate)
    {
        this.trainingService = trainingService;
        this.userAccountService = userAccountService;
        this.trainingTypeDao = trainingTypeDao;
        this.trainingDao = trainingDao;
        this.trainerDao = trainerDao;
        this.traineeDao = traineeDao;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Before
    public void beforeEach()
    {
        jdbcTemplate.update("DELETE FROM trainings");
    }

    @Given("valid trainer and trainee data is provided")
    public void validUserDataIsProvided()
    {
        transactionTemplate.executeWithoutResult(status -> {
            TrainingTypeEntity yoga =
                    trainingTypeDao.findByName("Yoga")
                            .orElseThrow();

            trainer = new TrainerEntity();
            trainer.setFirstName("John");
            trainer.setLastName("Doe");
            trainer.setSpecialization(yoga);
            trainer.setRole(Role.TRAINER);

            userAccountService.initializeNewAccount(trainer);

            trainerDao.create(trainer);

            trainee = new TraineeEntity();
            trainee.setFirstName("Alice");
            trainee.setLastName("Brown");
            trainee.setDateOfBirth(LocalDate.now());
            trainee.setAddress("address");
            trainee.setRole(Role.TRAINEE);

            userAccountService.initializeNewAccount(trainee);

            traineeDao.create(trainee);
        });
    }

    @When("valid training is created")
    public void validTrainingIsCreated()
    {
        training = new TrainingEntity();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("SomeTraining");
        training.setTrainingDate(LocalDate.of(2026, 9, 10));
        training.setTrainingDuration(23);
        training.setTrainingType(trainer.getSpecialization());

        trainingService.createTraining(training);
    }

    @Then("training creation succeeds")
    public void trainingCreated()
    {
        TrainingEntity saved =
                trainingDao.findById(training.getId())
                        .orElseThrow();

        assertEquals("SomeTraining", saved.getTrainingName());
        assertEquals(LocalDate.of(2026, 9, 10), saved.getTrainingDate());
        assertEquals(23, saved.getTrainingDuration());
        assertEquals(trainer.getId(), saved.getTrainer().getId());
        assertEquals(trainee.getId(), saved.getTrainee().getId());
    }

    @Given("invalid training is provided")
    public void invalidTrainingIsProvided()
    {
        training = new TrainingEntity();
        training.setTrainee(null);
        training.setTrainer(null);
        training.setTrainingName("SomeTraining");
        training.setTrainingDate(LocalDate.of(2026, 9, 10));
        training.setTrainingDuration(23);
        training.setTrainingType(null);
    }

    @When("training creation is attempted")
    public void trainingIsCreated()
    {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.createTraining(training));
    }

    @Then("request is rejected")
    public void trainingIsRejected()
    {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trainings", Integer.class);

        assertEquals(0, count);
    }
}
