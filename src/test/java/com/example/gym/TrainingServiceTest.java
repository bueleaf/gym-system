package com.example.gym;

import com.example.gym.daos.TrainingDao;
import com.example.gym.entities.*;
import com.example.gym.services.TrainingService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock private TrainingDao trainingDao;
    @InjectMocks private TrainingService trainingService;

    private TrainingEntity buildTraining(Long id, String name) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(1L);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(2L);
        trainer.setFirstName("Mike");
        trainer.setLastName("Smith");
        trainer.setSpecialization(new TrainingTypeEntity("Yoga"));

        TrainingTypeEntity type = new TrainingTypeEntity("Yoga");

        TrainingEntity training = new TrainingEntity();
        training.setId(id);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(name);
        training.setTrainingType(type);
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);
        return training;
    }

    @Test
    void createTraining_shouldPersist() {
        TrainingEntity t = buildTraining(null,"Morning Yoga");

        TrainingEntity result = trainingService.createTraining(t);

        verify(trainingDao).create(t);
        assertThat(result).isSameAs(t);
    }

    @Test
    void createTraining_shouldThrowWhenTraineeNull() {
        TrainingEntity t = buildTraining(null,"Test");
        t.setTrainee(null);

        assertThatThrownBy(() -> trainingService.createTraining(t))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getTraining_shouldReturnEntity() {
        TrainingEntity t = buildTraining(1L,"Morning Yoga");
        when(trainingDao.findById(1L)).thenReturn(Optional.of(t));

        assertThat(trainingService.getTraining(1L)).isSameAs(t);
    }

    @Test
    void getTraining_shouldThrowWhenNotFound() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.getTraining(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getAllTrainings_shouldReturnList() {
        when(trainingDao.findAll()).thenReturn(List.of(
                buildTraining(1L,"Yoga"),
                buildTraining(2L,"Cardio")
        ));

        assertThat(trainingService.getAllTrainings()).hasSize(2);
    }
}
