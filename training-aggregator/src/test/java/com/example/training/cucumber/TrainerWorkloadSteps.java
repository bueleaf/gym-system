package com.example.training.cucumber;

import com.example.training.consumer.TrainerWorkloadListener;
import com.example.training.dto.request.TrainerWorkloadEvent;
import com.example.training.dto.response.TrainerMonthlyWorkloadResponse;
import com.example.training.model.ActionType;
import com.example.training.repository.TrainerMonthlySummaryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.jms.Message;
import jakarta.validation.ConstraintViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainerWorkloadSteps
{
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    private final TrainerMonthlySummaryRepository repository;
    private final TrainerWorkloadListener listener;

    private String username;
    private TrainerWorkloadEvent event;
    private Exception thrownException;

    private static final Integer MONTH = 9;
    private static final Integer YEAR = 2026;

    public TrainerWorkloadSteps(
            ObjectMapper objectMapper,
            MockMvc mockMvc,
            TrainerMonthlySummaryRepository repository,
            TrainerWorkloadListener listener
    )
    {
        this.objectMapper = objectMapper;
        this.mockMvc = mockMvc;
        this.repository = repository;
        this.listener = listener;
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
    }

    @When("ADD workload event of {int} minutes is processed")
    public void addEventOfMinutes(int minutes) throws Exception
    {
        TrainerWorkloadEvent workload = new TrainerWorkloadEvent(
                username,
                "RandomName",
                "RandomSurname",
                true,
                LocalDate.of(YEAR, MONTH, 10),
                minutes,
                ActionType.ADD
        );

        String json = objectMapper.writeValueAsString(workload);
        Message message = mock(Message.class);

        listener.receive(json, message);
}

    @Then("trainer workload should contain {int} minutes")
    public void checkMinutes(int expectedMinutes) throws Exception
    {
        MvcResult result = mockMvc.perform(
                get("/api/workloads/{username}", username)
                        .param("month", MONTH.toString())
                        .param("year", YEAR.toString())
                        .with(jwt()
                                .jwt(jwt ->
                                        jwt.subject(username))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_TRAINER")
                                )
                        )
        ).andExpect(status().isOk()).andReturn();

        TrainerMonthlyWorkloadResponse response =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        TrainerMonthlyWorkloadResponse.class
                );

        assertEquals(expectedMinutes, response.trainingDurationTotal());
    }

    @Given("workload event without trainer username")
    public void workloadEventWithoutUsername()
    {
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
    public void workloadEventIsProcessed() throws Exception
    {
        String json = objectMapper.writeValueAsString(event);
        Message message = mock(Message.class);

        try
        {
            listener.receive(json, message);
        }
        catch (ConstraintViolationException ex)
        {
            thrownException = ex;
        }
    }

    @Then("workload event is rejected")
    public void workloadEventIsRejected()
    {
        assertInstanceOf(
                ConstraintViolationException.class,
                thrownException
        );
    }

    @Then("no trainer workload is created")
    public void noTrainerWorkloadIsCreated() throws Exception
    {
        MvcResult result = mockMvc.perform(
                get("/api/workloads/{username}", username)
                        .param("month", MONTH.toString())
                        .param("year", YEAR.toString())
                        .with(jwt()
                                .jwt(jwt ->
                                        jwt.subject("trainer.test"))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_TRAINER")
                                )
                        )
        ).andExpect(status().isNotFound()).andReturn();
    }
}
