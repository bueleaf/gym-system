package com.example.gym;

import com.example.gym.controller.AuthController;
import com.example.gym.controller.TraineeController;
import com.example.gym.controller.TrainingController;
import com.example.gym.dto.response.ApiErrorResponse;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.exception.GlobalExceptionHandler;
import com.example.gym.facade.GymFacade;
import com.example.gym.dto.request.TraineeTrainingSearchCriteria;
import com.example.gym.dto.request.TrainerTrainingSearchCriteria;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ControllerRequestShapeTest {

    @Mock
    private GymFacade gymFacade;

    private MockMvc authMvc;
    private MockMvc traineeMvc;
    private MockMvc trainingMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController();
        authController.getGymFacade(gymFacade);

        TraineeController traineeController = new TraineeController();
        traineeController.setGymFacade(gymFacade);

        TrainingController trainingController = new TrainingController();
        trainingController.setGymFacade(gymFacade);

        GlobalExceptionHandler exceptionHandler =
                new GlobalExceptionHandler();
        objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MappingJackson2HttpMessageConverter jsonConverter =
                new MappingJackson2HttpMessageConverter(
                        objectMapper
                );

        authMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(jsonConverter)
                .build();

        traineeMvc = MockMvcBuilders
                .standaloneSetup(traineeController)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(jsonConverter)
                .build();

        trainingMvc = MockMvcBuilders
                .standaloneSetup(trainingController)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(jsonConverter)
                .build();
    }

    @Test
    void login_usesUsernameAndPasswordQueryParameters() throws Exception {
        authMvc.perform(get("/api/login")
                        .param("username", "john.doe")
                        .param("password", "pass"))
                .andExpect(status().isOk());

        verify(gymFacade).authenticateUser("john.doe", "pass");
    }

    @Test
    void getTraineeProfile_usesUsernameAndPasswordQueryParameters()
            throws Exception {

        TraineeEntity trainee = trainee("john.doe");
        when(gymFacade.getTraineeByUsername("john.doe", "pass"))
                .thenReturn(trainee);

        traineeMvc.perform(get("/api/trainees")
                        .param("username", "john.doe")
                        .param("password", "pass"))
                .andExpect(status().isOk());

        verify(gymFacade).getTraineeByUsername("john.doe", "pass");
    }

    @Test
    void updateTraineeProfile_usesUsernameAndPasswordFromBody()
            throws Exception {

        TraineeEntity trainee = trainee("john.doe");
        trainee.setFirstName("John");
        trainee.setLastName("Smith");
        trainee.setActive(false);

        when(gymFacade.updateTraineeProfile(
                eq("john.doe"),
                eq("pass"),
                any()))
                .thenReturn(trainee);

        traineeMvc.perform(put("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "john.doe",
                                  "password": "pass",
                                  "firstName": "John",
                                  "lastName": "Smith",
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk());

        verify(gymFacade).updateTraineeProfile(
                eq("john.doe"),
                eq("pass"),
                any());
    }

    @Test
    void getTraineeTrainings_usesUsernameAndPasswordQueryParameters()
            throws Exception {

        when(gymFacade.getTraineeTrainings(
                eq("john.doe"),
                eq("pass"),
                any()))
                .thenReturn(List.of());

        trainingMvc.perform(get("/api/trainee-trainings")
                        .param("username", "john.doe")
                        .param("password", "pass")
                        .param("fromDate", "2026-01-01")
                        .param("toDate", "2026-01-31")
                        .param("trainerName", "Mike")
                        .param("trainingTypeName", "Yoga"))
                .andExpect(status().isOk());

        ArgumentCaptor<TraineeTrainingSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(TraineeTrainingSearchCriteria.class);

        verify(gymFacade).getTraineeTrainings(
                eq("john.doe"),
                eq("pass"),
                criteriaCaptor.capture());

        assertThat(criteriaCaptor.getValue().getTrainerName())
                .isEqualTo("Mike");
        assertThat(criteriaCaptor.getValue().getTrainingTypeName())
                .isEqualTo("Yoga");
    }

    @Test
    void getTrainerTrainings_usesDateAndTraineeFilters()
            throws Exception {

        when(gymFacade.getTrainerTrainings(
                eq("mike.smith"),
                eq("pass"),
                any()))
                .thenReturn(List.of());

        trainingMvc.perform(get("/api/trainer-trainings")
                        .param("username", "mike.smith")
                        .param("password", "pass")
                        .param("fromDate", "2026-01-01")
                        .param("toDate", "2026-01-31")
                        .param("traineeName", "John"))
                .andExpect(status().isOk());

        ArgumentCaptor<TrainerTrainingSearchCriteria> criteriaCaptor =
                ArgumentCaptor.forClass(TrainerTrainingSearchCriteria.class);

        verify(gymFacade).getTrainerTrainings(
                eq("mike.smith"),
                eq("pass"),
                criteriaCaptor.capture());

        assertThat(criteriaCaptor.getValue().getTraineeName())
                .isEqualTo("John");
    }

    @Test
    void traineeRegistration_invalidLocalDateReturnsSafeMessage()
            throws Exception {

        MvcResult result = traineeMvc.perform(post("/api/trainees/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Test",
                                  "dateOfBirth": "not-a-date",
                                  "address": "Main Street"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(errorResponse(result).message())
                .isEqualTo("Invalid value for dateOfBirth. Expected format: yyyy-MM-dd");
    }

    @Test
    void traineeRegistration_malformedJsonReturnsSafeMessage()
            throws Exception {

        MvcResult result = traineeMvc.perform(post("/api/trainees/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Test"
                                """))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(errorResponse(result).message())
                .isEqualTo("Malformed request body or invalid field format");
    }

    @Test
    void getTraineeTrainings_invalidDateRangeReturnsBadRequest()
            throws Exception {

        MvcResult result = trainingMvc.perform(get("/api/trainee-trainings")
                        .param("username", "john.doe")
                        .param("password", "pass")
                        .param("fromDate", "2026-08-01")
                        .param("toDate", "2026-07-31"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(errorResponse(result).message())
                .isEqualTo("From date cannot be after to date");
    }

    @Test
    void getTraineeTrainings_invalidDateFormatReturnsSafeMessage()
            throws Exception {

        MvcResult result = trainingMvc.perform(get("/api/trainee-trainings")
                        .param("username", "john.doe")
                        .param("password", "pass")
                        .param("fromDate", "bad-date"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(errorResponse(result).message())
                .isEqualTo("Invalid value for fromDate. Expected format: yyyy-MM-dd");
    }

    private ApiErrorResponse errorResponse(MvcResult result)
            throws Exception {

        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ApiErrorResponse.class
        );
    }

    private TraineeEntity trainee(String username) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUsername(username);
        trainee.setPassword("pass");
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setActive(true);
        return trainee;
    }
}
