package com.example.gym;

import com.example.gym.actuator.metric.MetricsWrapper;
import com.example.gym.entity.*;
import com.example.gym.facade.GymFacade;
import com.example.gym.dto.request.AddTrainingRequest;
import com.example.gym.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;
    @Mock private TrainingTypeService trainingTypeService;
    @Mock private AuthenticationService authenticationService;
    @Mock private MetricsWrapper metricsWrapper;

    @InjectMocks private GymFacade gymFacade;

    private TraineeEntity buildTrainee() {
        TraineeEntity t = new TraineeEntity();
        t.setId(1L);
        t.setFirstName("John");
        t.setLastName("Doe");
        t.setUsername("john.doe");
        t.setPassword("pass");
        t.setActive(true);
        return t;
    }

    private TrainerEntity buildTrainer() {
        TrainerEntity tr = new TrainerEntity();
        tr.setId(2L);
        tr.setFirstName("Mike");
        tr.setLastName("Smith");
        tr.setUsername("mike.smith");
        tr.setPassword("pass");
        tr.setActive(true);
        tr.setSpecialization(new TrainingTypeEntity("Yoga"));
        return tr;
    }

    @Test
    void createTrainerProfile_success() {
        TrainingTypeEntity type = new TrainingTypeEntity("Yoga");
        TrainerEntity trainer = buildTrainer();

        when(trainingTypeService.getTrainingTypeByName("Yoga")).thenReturn(type);
        when(trainerService.createTrainer(any())).thenReturn(trainer);

        TrainerEntity result = gymFacade.createTrainerProfile("Mike","Smith","Yoga");

        assertThat(result).isSameAs(trainer);
        verify(trainerService).createTrainer(any());
        verify(metricsWrapper).recordTrainerRegistered();
    }

    @Test
    void createTrainerProfile_invalidType() {
        when(trainingTypeService.getTrainingTypeByName("Invalid"))
                .thenThrow(new IllegalArgumentException("Invalid training type: Invalid"));

        assertThatThrownBy(() ->
                gymFacade.createTrainerProfile("Mike","Smith","Invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createTraineeProfile_success() {
        TraineeEntity trainee = buildTrainee();
        when(traineeService.createTrainee(any())).thenReturn(trainee);

        TraineeEntity result = gymFacade.createTraineeProfile(
                "John","Doe", LocalDate.of(1990,1,1),"Address");

        assertThat(result).isSameAs(trainee);
        verify(traineeService).createTrainee(any());
        verify(metricsWrapper).recordTraineeRegistered();
    }

    @Test
    void authenticateUser_delegates() {
        TraineeEntity trainee = buildTrainee();
        when(authenticationService.authenticate("john.doe","pass")).thenReturn(trainee);

        UserEntity result = gymFacade.authenticateUser("john.doe","pass");
        assertThat(result).isSameAs(trainee);
    }

    @Test
    void getTraineeByUsername_returnsOnlyTrainee() {
        TraineeEntity trainee = buildTrainee();
        when(authenticationService.authenticate("john.doe","pass")).thenReturn(trainee);
        when(traineeService.getTraineeProfileByUsername("john.doe")).thenReturn(trainee);

        assertThat(gymFacade.getTraineeByUsername("john.doe","pass")).isSameAs(trainee);
    }

    @Test
    void getTraineeByUsername_throwsIfNotTrainee() {
        TrainerEntity trainer = buildTrainer();
        when(authenticationService.authenticate("mike.smith","pass")).thenReturn(trainer);

        assertThatThrownBy(() ->
                gymFacade.getTraineeByUsername("mike.smith","pass"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void changeTraineePassword_delegates() {
        TraineeEntity trainee = buildTrainee();
        when(authenticationService.authenticate("john.doe","old")).thenReturn(trainee);

        gymFacade.changeTraineePassword("john.doe","old","new");
        verify(traineeService).changePassword("john.doe","new");
    }

    @Test
    void addTraining_delegates() {
        TraineeEntity trainee = buildTrainee();
        when(authenticationService.authenticate("john.doe","pass")).thenReturn(trainee);

        TraineeEntity targetTrainee = buildTrainee();
        TrainerEntity targetTrainer = buildTrainer();

        AddTrainingRequest request = new AddTrainingRequest(
                "john.doe",
                "pass",
                "john.doe",
                "mike.smith",
                "Morning Yoga",
                LocalDate.of(2026, 1, 1),
                60
        );

        when(traineeService.getTraineeByUsername("john.doe"))
                .thenReturn(targetTrainee);
        when(trainerService.getTrainerByUsername("mike.smith"))
                .thenReturn(targetTrainer);
        when(trainingService.createTraining(any())).thenAnswer(invocation ->
                invocation.getArgument(0));

        TrainingEntity result = gymFacade.addTraining("john.doe","pass",request);

        assertThat(result.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(result.getTrainee()).isSameAs(targetTrainee);
        assertThat(result.getTrainer()).isSameAs(targetTrainer);
        verify(metricsWrapper).recordTrainingCreated();
    }

    @Test
    void getUnassignedTrainers_returnsList() {
        TraineeEntity trainee = buildTrainee();
        when(authenticationService.authenticate("john.doe","pass")).thenReturn(trainee);

        when(traineeService.getUnassignedTrainers("john.doe"))
                .thenReturn(List.of(buildTrainer()));

        assertThat(gymFacade.getUnassignedTrainers("john.doe","pass")).hasSize(1);
    }
}
