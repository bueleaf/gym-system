package com.example.integration.cucumber;

import com.example.integration.dto.request.AddTrainingRequest;
import com.example.integration.dto.request.TraineeRegistrationRequest;
import com.example.integration.dto.request.TrainerRegistrationRequest;
import com.example.integration.dto.response.CredentialsResponse;
import com.example.integration.dto.response.LoginResponse;
import com.example.integration.dto.response.TrainerMonthlyWorkloadResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Before;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class WorkloadMessagingSteps
{
    private RestClient gymRestClient()
    {
        return RestClient.create(MessagingEnvironment.gymUrl());
    }

    private RestClient aggregatorRestClient()
    {
        return RestClient.create(MessagingEnvironment.aggregatorUrl());
    }

    private String trainerUsername;
    private String traineeUsername;
    private CredentialsResponse response;
    private LoginResponse loginResponse;
    private String suffix;

    @Before
    public void beforeScenario()
    {
        suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }

    @Given("trainee and trainer exist in Gym")
    public void trainerExists()
    {
        TrainerRegistrationRequest requestTrainer = new TrainerRegistrationRequest(
                "trainer" + suffix,
                "test",
                "Yoga"
        );

        response = gymRestClient().post()
                .uri("api/trainers/registration")
                .body(requestTrainer)
                .retrieve()
                .body(CredentialsResponse.class);

        trainerUsername = response.username();

        TraineeRegistrationRequest requestTrainee = new TraineeRegistrationRequest(
                "trainee" + suffix,
                "test",
                LocalDate.now(),
                "address"
        );

        loginResponse = gymRestClient().post()
                .uri("api/login")
                .body(response)
                .retrieve()
                .body(LoginResponse.class);

        traineeUsername = gymRestClient().post()
                .uri("api/trainees/registration")
                .body(requestTrainee)
                .retrieve()
                .body(CredentialsResponse.class)
                .username();
    }

    @Given("no workload exists for the trainer")
    public void noWorkloadExistsForTrainer()
    {
        assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> aggregatorRestClient().get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/workloads/{username}")
                                .queryParam("year", 2026)
                                .queryParam("month", 9)
                                .build(trainerUsername))
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                loginResponse.type() + " " + loginResponse.token()
                        )
                        .retrieve()
                        .toBodilessEntity()
        );
    }

    @When("{int} minute training is created in Gym service")
    public void trainingIsCreatedInGymService(int minutes)
    {
        AddTrainingRequest request = new AddTrainingRequest(
                traineeUsername,
                "yoga training",
                LocalDate.of(2026, 9, 10),
                minutes
        );

        gymRestClient().post()
                .uri("api/trainings")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        loginResponse.type() + " " + loginResponse.token()
                )
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    @Then("Aggregator service contains {int} minutes of workload")
    public void aggregatorContainsWorkload(int minutes)
    {

        await()
                .atMost(Duration.ofSeconds(5))
                        .untilAsserted(() -> {
                            TrainerMonthlyWorkloadResponse response =
                                    aggregatorRestClient().get()
                                            .uri(uriBuilder -> uriBuilder
                                                    .path("/api/workloads/{username}")
                                                    .queryParam("year", 2026)
                                                    .queryParam("month", 9)
                                                    .build(trainerUsername))
                                            .header(
                                                    HttpHeaders.AUTHORIZATION,
                                                    loginResponse.type() + " " + loginResponse.token()
                                            )
                                            .retrieve()
                                            .body(TrainerMonthlyWorkloadResponse.class);

                            assertNotNull(response);
                            assertEquals(minutes, response.trainingDurationTotal());
                        });

    }

    @When("invalid training creation is attempted")
    public void invalidTrainingCreationIsAttempted()
    {
        AddTrainingRequest request = new AddTrainingRequest(
                traineeUsername,
                "yoga training",
                LocalDate.of(2026, 9, 10),
                null
        );

        assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> gymRestClient().post()
                        .uri("api/trainings")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                loginResponse.type() + " " + loginResponse.token()
                        )
                        .body(request)
                        .retrieve()
                        .toBodilessEntity()
        );
    }

    @Then("Aggregator service does not update workload for the trainer")
    public void aggregatorWorkloadNotUpdated()
    {
        await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThrows(HttpClientErrorException.NotFound.class,
                            () -> aggregatorRestClient().get()
                                    .uri(uriBuilder -> uriBuilder
                                            .path("/api/workloads/{username}")
                                            .queryParam("year", 2026)
                                            .queryParam("month", 9)
                                            .build(trainerUsername))
                                    .header(
                                            HttpHeaders.AUTHORIZATION,
                                            loginResponse.type() + " " + loginResponse.token()
                                    )
                                    .retrieve()
                                    .toBodilessEntity());
                });
    }
}
