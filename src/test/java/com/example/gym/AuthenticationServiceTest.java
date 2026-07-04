package com.example.gym;

import com.example.gym.daos.UserDao;
import com.example.gym.entities.*;
import com.example.gym.services.AuthenticationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private AuthenticationService authenticationService;

    private TraineeEntity buildTrainee(String username, String password) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(1L);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("123 Main St");
        return trainee;
    }

    private TrainerEntity buildTrainer(String username, String password) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(2L);
        trainer.setFirstName("Mike");
        trainer.setLastName("Smith");
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setActive(true);
        trainer.setSpecialization(new TrainingTypeEntity("Yoga")); // no setId
        return trainer;
    }

    @Test
    void shouldAuthenticateTraineeWithCorrectCredentials() {
        TraineeEntity trainee = buildTrainee("john.doe", "pass");
        when(userDao.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        UserEntity result = authenticationService.authenticate("john.doe", "pass");
        assertThat(result).isSameAs(trainee);
    }

    @Test
    void shouldThrowForWrongPassword() {
        TraineeEntity trainee = buildTrainee("john.doe", "pass");
        when(userDao.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() ->
                authenticationService.authenticate("john.doe", "wrong"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void shouldAuthenticateTrainerWithCorrectCredentials() {
        TrainerEntity trainer = buildTrainer("mike.smith", "pass");
        when(userDao.findByUsername("mike.smith")).thenReturn(Optional.of(trainer));

        UserEntity result = authenticationService.authenticate("mike.smith", "pass");
        assertThat(result).isSameAs(trainer);
    }

    @Test
    void shouldThrowWhenUsernameNotFound() {
        when(userDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authenticationService.authenticate("unknown", "pass"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void shouldQueryUserDao() {
        TraineeEntity trainee = buildTrainee("john.doe", "pass");
        when(userDao.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        authenticationService.authenticate("john.doe", "pass");
        verify(userDao).findByUsername("john.doe");
    }
}