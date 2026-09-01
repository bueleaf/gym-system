package com.example.gym.service;

import com.example.gym.actuator.metric.MetricsWrapper;
import com.example.gym.application.TrainingManagementService;
import com.example.gym.dto.TraineeTrainingSearchCriteria;
import com.example.gym.dto.TrainerTrainingSearchCriteria;
import com.example.gym.dto.request.AddTrainingRequest;
import com.example.gym.dto.request.TrainerWorkloadEvent;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.entity.TrainingTypeEntity;
import com.example.gym.model.ActionType;
import com.example.gym.producer.TrainerWorkloadProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingManagementServiceTest {
    @Mock private TrainingService trainingService;
    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private MetricsWrapper metricsWrapper;
    @Mock private TrainerWorkloadProducer producer;

    @InjectMocks
    private TrainingManagementService trainingManagementService;

    @Test
    void addTrainingCreatesTrainingRecordsMetricAndUpdatesWorkload() {
        AddTrainingRequest request = new AddTrainingRequest(
                "trainee", "Morning session", LocalDate.of(2026, 8, 1), 60);
        TraineeEntity trainee = new TraineeEntity();
        TrainerEntity trainer = trainer("trainer", "Jane", "Doe", true);
        TrainingEntity created = new TrainingEntity();
        created.setTrainingDate(request.trainingDate());
        created.setTrainingDuration(request.trainingDuration());
        when(traineeService.getTraineeByUsername("trainee")).thenReturn(trainee);
        when(trainerService.getTrainerByUsername("trainer")).thenReturn(trainer);
        when(trainingService.createTraining(org.mockito.ArgumentMatchers.any(TrainingEntity.class)))
                .thenReturn(created);

        trainingManagementService.addTraining("trainer", request);

        ArgumentCaptor<TrainingEntity> trainingCaptor = ArgumentCaptor.forClass(TrainingEntity.class);
        verify(trainingService).createTraining(trainingCaptor.capture());
        TrainingEntity training = trainingCaptor.getValue();
        assertThat(training.getTrainee()).isSameAs(trainee);
        assertThat(training.getTrainer()).isSameAs(trainer);
        assertThat(training.getTrainingName()).isEqualTo("Morning session");
        assertThat(training.getTrainingDate()).isEqualTo(request.trainingDate());
        assertThat(training.getTrainingDuration()).isEqualTo(60);
        assertThat(training.getTrainingType()).isSameAs(trainer.getSpecialization());
        verify(metricsWrapper).recordTrainingCreated();
        verify(producer).send(new TrainerWorkloadEvent(
                "trainer", "Jane", "Doe", true, request.trainingDate(), 60, ActionType.ADD));
    }

    @Test
    void deleteTrainingDeletesAndUpdatesWorkloadForOwningTrainer() {
        TrainerEntity trainingTrainer = trainer("trainer", "Jane", "Doe", true);
        trainingTrainer.setId(7L);
        TrainerEntity authenticatedTrainer = trainer("trainer", "Jane", "Doe", true);
        authenticatedTrainer.setId(7L);
        TrainingEntity training = training(trainingTrainer);
        when(trainingService.getTraining(42L)).thenReturn(training);
        when(trainerService.getTrainerByUsername("trainer")).thenReturn(authenticatedTrainer);

        trainingManagementService.deleteTraining("trainer", 42L);

        verify(trainingService).deleteTraining(42L);
        verify(producer).send(new TrainerWorkloadEvent(
                "trainer", "Jane", "Doe", true, LocalDate.of(2026, 8, 1), 60, ActionType.DELETE));
    }

    @Test
    void deleteTrainingRejectsAnotherTrainer() {
        TrainingEntity training = training(trainer("owner", "Jane", "Doe", true));
        when(trainingService.getTraining(42L)).thenReturn(training);
        when(trainerService.getTrainerByUsername("other"))
                .thenReturn(trainer("other", "John", "Smith", true));

        assertThatThrownBy(() -> trainingManagementService.deleteTraining("other", 42L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Training doesn't belong to trainer");

        verify(trainingService, never()).deleteTraining(42L);
        verify(producer, never()).send(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getTraineeTrainingsDelegatesCriteriaToTrainingService() {
        TraineeTrainingSearchCriteria criteria = new TraineeTrainingSearchCriteria(null, null, null, null);
        List<TrainingEntity> expected = List.of(new TrainingEntity());
        when(trainingService.getTraineeTrainingsByCriteria("trainee", criteria)).thenReturn(expected);

        assertThat(trainingManagementService.getTraineeTrainings("trainee", criteria)).isSameAs(expected);
    }

    @Test
    void getTrainerTrainingsDelegatesCriteriaToTrainingService() {
        TrainerTrainingSearchCriteria criteria = new TrainerTrainingSearchCriteria(null, null, null);
        List<TrainingEntity> expected = List.of(new TrainingEntity());
        when(trainingService.getTrainerTrainingsByCriteria("trainer", criteria)).thenReturn(expected);

        assertThat(trainingManagementService.getTrainerTrainings("trainer", criteria)).isSameAs(expected);
    }

    private TrainerEntity trainer(String username, String firstName, String lastName, boolean active) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUsername(username);
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setActive(active);
        trainer.setSpecialization(new TrainingTypeEntity("Fitness"));
        return trainer;
    }

    private TrainingEntity training(TrainerEntity trainer) {
        TrainingEntity training = new TrainingEntity();
        training.setTrainer(trainer);
        training.setTrainingDate(LocalDate.of(2026, 8, 1));
        training.setTrainingDuration(60);
        return training;
    }
}
