package com.example.gym;

import com.example.gym.daos.*;
import com.example.gym.dtos.UpdateTraineeProfileRequest;
import com.example.gym.entities.*;
import com.example.gym.services.*;
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
class TraineeServiceTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private UserAccountService userAccountService;

    @InjectMocks private TraineeService traineeService;

    private TraineeEntity buildTrainee(Long id, String first, String last) {
        TraineeEntity t = new TraineeEntity();
        t.setId(id);
        t.setFirstName(first);
        t.setLastName(last);
        t.setActive(true);
        t.setDateOfBirth(LocalDate.of(1990,1,1));
        t.setAddress("Address");
        return t;
    }

    @Test
    void createTrainee_shouldInitializeAndPersist() {
        TraineeEntity t = buildTrainee(null,"John","Doe");

        doAnswer(inv -> {
            TraineeEntity tr = inv.getArgument(0);
            tr.setUsername("John.Doe");
            tr.setPassword("pass");
            tr.setActive(true);
            return null;
        }).when(userAccountService).initializeNewAccount(any());

        TraineeEntity result = traineeService.createTrainee(t);

        assertThat(result.getUsername()).isEqualTo("John.Doe");
        verify(traineeDao).create(t);
    }

    @Test
    void createTrainee_shouldRejectMissingAddress() {
        TraineeEntity t = buildTrainee(null, "John", "Doe");
        t.setAddress(null);

        assertThatThrownBy(() -> traineeService.createTrainee(t))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Address is required");
    }

    @Test
    void createTrainee_shouldRejectMissingDateOfBirth() {
        TraineeEntity t = buildTrainee(null, "John", "Doe");
        t.setDateOfBirth(null);

        assertThatThrownBy(() -> traineeService.createTrainee(t))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Date of birth is required");
    }

    @Test
    void updateOwnProfile_shouldUpdateFields() {
        TraineeEntity existing = buildTrainee(1L,"Old","User");
        existing.setUsername("john.doe");

        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(existing));
        when(traineeDao.update(existing)).thenReturn(existing);

        UpdateTraineeProfileRequest req = new UpdateTraineeProfileRequest(
                "John","Smith", LocalDate.of(1992,2,2),"New Address");

        TraineeEntity result = traineeService.updateOwnProfile("john.doe", req);

        assertThat(result.getFirstName()).isEqualTo("John");
        verify(traineeDao).update(existing);
    }

    @Test
    void updateOwnProfile_shouldRejectMissingAddress() {
        TraineeEntity existing = buildTrainee(1L, "Old", "User");
        existing.setUsername("john.doe");

        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(existing));

        UpdateTraineeProfileRequest req = new UpdateTraineeProfileRequest(
                "John", "Smith", LocalDate.of(1992, 2, 2), " ");

        assertThatThrownBy(() -> traineeService.updateOwnProfile("john.doe", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Address is required");
    }

    @Test
    void updateOwnProfile_shouldRejectMissingDateOfBirth() {
        TraineeEntity existing = buildTrainee(1L, "Old", "User");
        existing.setUsername("john.doe");

        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(existing));

        UpdateTraineeProfileRequest req = new UpdateTraineeProfileRequest(
                "John", "Smith", null, "New Address");

        assertThatThrownBy(() -> traineeService.updateOwnProfile("john.doe", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Date of birth is required");
    }

    @Test
    void updateOwnProfile_notFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                traineeService.updateOwnProfile("unknown",
                        new UpdateTraineeProfileRequest("x","y",LocalDate.now(),"a")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void changePassword_shouldDelegate() {
        TraineeEntity t = buildTrainee(1L,"John","Doe");
        t.setUsername("john.doe");

        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(t));
        when(traineeDao.update(t)).thenReturn(t);

        traineeService.changePassword("john.doe","new");
        verify(userAccountService).changePassword(t,"new");
        verify(traineeDao).update(t);
    }

    @Test
    void deleteTrainee_shouldDeleteById() {
        TraineeEntity t = buildTrainee(1L,"John","Doe");
        t.setUsername("john.doe");

        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(t));

        traineeService.deleteTraineeByUsername("john.doe");
        verify(traineeDao).delete(1L);
    }

    @Test
    void updateTraineeTrainers_shouldReplaceList() {
        TraineeEntity trainee = buildTrainee(1L,"John","Doe");
        trainee.setUsername("john.doe");

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(1L);
        trainer.setUsername("mike.smith");
        trainer.setFirstName("Mike");
        trainer.setLastName("Smith");
        trainer.setActive(true);
        trainer.setSpecialization(new TrainingTypeEntity("Yoga"));

        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("mike.smith")).thenReturn(Optional.of(trainer));

        List<TrainerEntity> result =
                traineeService.updateTraineeTrainers("john.doe", List.of("mike.smith"));

        assertThat(result).hasSize(1);
        verify(traineeDao).update(trainee);
    }

    @Test
    void getUnassignedTrainers_shouldFilter() {
        TraineeEntity trainee = buildTrainee(1L,"John","Doe");
        trainee.setUsername("john.doe");

        TrainerEntity assigned = new TrainerEntity();
        assigned.setId(1L);
        assigned.setFirstName("Mike");
        assigned.setLastName("Smith");
        assigned.setActive(true);
        assigned.setSpecialization(new TrainingTypeEntity("Yoga"));

        TrainerEntity unassigned = new TrainerEntity();
        unassigned.setId(2L);
        unassigned.setFirstName("Sara");
        unassigned.setLastName("Jones");
        unassigned.setActive(true);
        unassigned.setSpecialization(new TrainingTypeEntity("Cardio"));

        trainee.addTrainer(assigned);

        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findAll()).thenReturn(Arrays.asList(assigned, unassigned));

        List<TrainerEntity> result = traineeService.getUnassignedTrainers("john.doe");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Sara");
    }
}
