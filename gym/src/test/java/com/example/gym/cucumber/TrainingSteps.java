package com.example.gym.cucumber;

import com.example.gym.dto.request.AddTrainingRequest;
import com.example.gym.dto.request.TraineeRegistrationRequest;
import com.example.gym.dto.request.TrainerRegistrationRequest;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.dto.response.TrainerTrainingResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainingSteps
{
    private final MockMvc mockMvc;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private String trainerUsername;
    private String traineeUsername;
    private AddTrainingRequest invalid;
    private ResultActions result;

    public TrainingSteps(
            MockMvc mockMvc,
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    )
    {
        this.mockMvc = mockMvc;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Before
    public void beforeEach()
    {
        jdbcTemplate.update("DELETE FROM trainings");
    }

    @Given("valid trainer and trainee data is provided")
    public void validUserDataIsProvided() throws Exception
    {
        TrainerRegistrationRequest requestTrainer = new TrainerRegistrationRequest(
                "trainer",
                "test",
                "Yoga"
        );

        MvcResult trainerResult = mockMvc.perform(
                post("/api/trainers/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                requestTrainer
                        ))
                ).andReturn();

        CredentialsResponse tempResponse = objectMapper.readValue(
                trainerResult.getResponse().getContentAsString(),
                CredentialsResponse.class
        );

        trainerUsername = tempResponse.username();

        TraineeRegistrationRequest requestTrainee = new TraineeRegistrationRequest(
                "trainee",
                "test",
                LocalDate.now(),
                "address"
        );

        MvcResult traineeResult = mockMvc.perform(
                post("/api/trainees/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                requestTrainee
                        ))
        ).andReturn();

        tempResponse = objectMapper.readValue(
                traineeResult.getResponse().getContentAsString(),
                CredentialsResponse.class
        );

        traineeUsername = tempResponse.username();
    }

    @When("valid training is created")
    public void validTrainingIsCreated() throws Exception
    {
        AddTrainingRequest request = new AddTrainingRequest(
                traineeUsername,
                "yoga training",
                LocalDate.of(2026, 9, 10),
                60
        );

        mockMvc.perform(
                post("/api/trainings")
                        .with(jwt()
                                .jwt(jwt ->
                                jwt.subject(trainerUsername))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_TRAINER")
                                )
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                request
                        ))
        ).andExpect(status().isCreated());
    }

    @Then("training creation succeeds")
    public void trainingCreated() throws Exception
    {
        MvcResult result = mockMvc.perform(
                get("/api/trainer-trainings")
                        .with(jwt()
                                .jwt(jwt ->
                                        jwt.subject(trainerUsername))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_TRAINER")
                                )
                        )
                        .param("fromDate", "2026-09-10")
                        .param("toDate", "2026-09-10")
        ).andExpect(status().isOk()).andReturn();

        List<TrainerTrainingResponse> trainings =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {}
                );

        assertTrue(trainings.stream()
                .anyMatch(tr ->
                    tr.trainingName().equals("yoga training")
                            && tr.trainingDuration() == 60
                ));
    }

    @Given("invalid training is provided")
    public void invalidTrainingIsProvided()
    {
        invalid = new AddTrainingRequest(
                null,
                "yoga training",
                LocalDate.of(2026, 9, 10),
                60
        );
    }

    @When("training creation is attempted")
    public void trainingIsCreated() throws Exception
    {
        result = mockMvc.perform(
                post("/api/trainings")
                        .with(jwt()
                                .jwt(jwt ->
                                        jwt.subject(trainerUsername))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_TRAINER")
                                )
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                invalid
                        ))
        );
    }

    @Then("request is rejected")
    public void trainingIsRejected() throws Exception
    {
        result.andExpect(status().isBadRequest());
    }
}
