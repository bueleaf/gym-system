package com.example.gym;

import com.example.gym.daos.*;
import com.example.gym.dtos.UpdateTrainerProfileRequest;
import com.example.gym.entities.*;
import com.example.gym.services.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock private TrainerDao trainerDao;
    @Mock private TrainingTypeDao trainingTypeDao;
    @Mock private UserAccountService userAccountService;

    @InjectMocks private TrainerService trainerService;

    private TrainerEntity buildTrainer(Long id, String first, String last) {
        TrainerEntity t = new TrainerEntity();
        t.setId(id);
        t.setFirstName(first);
        t.setLastName(last);
        t.setActive(true);
        t.setSpecialization(new TrainingTypeEntity("Yoga"));
        return t;
    }

    @Test
    void createTrainer_shouldInitializeAccount() {
        TrainerEntity trainer = buildTrainer(null,"Mike","Smith");

        doAnswer(inv -> {
            TrainerEntity t = inv.getArgument(0);
            t.setUsername("Mike.Smith");
            t.setPassword("pass");
            t.setActive(true);
            return null;
        }).when(userAccountService).initializeNewAccount(any());

        TrainerEntity result = trainerService.createTrainer(trainer);

        assertThat(result.getUsername()).isEqualTo("Mike.Smith");
        verify(trainerDao).create(trainer);
    }

    @Test
    void createTrainer_shouldThrowWhenFirstNameNull() {
        TrainerEntity trainer = buildTrainer(null,null,"Smith");
        assertThatThrownBy(() -> trainerService.createTrainer(trainer))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createTrainer_shouldThrowWhenSpecializationNull() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setFirstName("Mike");
        trainer.setLastName("Smith");
        trainer.setSpecialization(null);

        assertThatThrownBy(() -> trainerService.createTrainer(trainer))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateOwnProfile_shouldUpdateTrainer() {
        TrainerEntity existing = buildTrainer(1L,"Old","Name");
        existing.setUsername("mike.smith");

        TrainingTypeEntity newType = new TrainingTypeEntity("Cardio");

        when(trainerDao.findByUsername("mike.smith")).thenReturn(Optional.of(existing));
        when(trainingTypeDao.findById(2L)).thenReturn(Optional.of(newType));
        when(trainerDao.update(existing)).thenReturn(existing);

        UpdateTrainerProfileRequest req = new UpdateTrainerProfileRequest("Mike","Smith",2L);

        TrainerEntity result = trainerService.updateOwnProfile("mike.smith", req);

        assertThat(result.getSpecialization()).isSameAs(newType);
        verify(trainerDao).update(existing);
    }

    @Test
    void updateOwnProfile_trainerNotFound() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                trainerService.updateOwnProfile("unknown",
                        new UpdateTrainerProfileRequest("a","b",1L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateOwnProfile_trainingTypeNotFound() {
        TrainerEntity existing = buildTrainer(1L,"Old","Name");
        existing.setUsername("mike.smith");

        when(trainerDao.findByUsername("mike.smith")).thenReturn(Optional.of(existing));
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                trainerService.updateOwnProfile("mike.smith",
                        new UpdateTrainerProfileRequest("a","b",99L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTrainerByUsername_shouldReturnTrainer() {
        TrainerEntity t = buildTrainer(1L,"Mike","Smith");
        t.setUsername("mike.smith");

        when(trainerDao.findByUsername("mike.smith")).thenReturn(Optional.of(t));
        assertThat(trainerService.getTrainerByUsername("mike.smith")).isSameAs(t);
    }

    @Test
    void getAllTrainers_shouldReturnList() {
        when(trainerDao.findAll()).thenReturn(List.of(
                buildTrainer(1L,"A","B"),
                buildTrainer(2L,"C","D")
        ));

        assertThat(trainerService.getAllTrainers()).hasSize(2);
    }

    @Test
    void getTrainingTypeByName_shouldReturnOptional() {
        TrainingTypeEntity type = new TrainingTypeEntity("Yoga");
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(type));

        assertThat(trainerService.getTrainingTypeByName("Yoga"))
                .isPresent();
    }
}