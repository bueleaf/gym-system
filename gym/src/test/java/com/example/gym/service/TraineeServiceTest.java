package com.example.gym.service;

import com.example.gym.dao.TraineeDao;
import com.example.gym.dao.TrainerDao;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {
    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private UserAccountService userAccountService;
    @InjectMocks private TraineeService traineeService;

    @Test
    void createTraineePersistsAccountAndReturnsCredentials() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        CredentialsResponse credentials = new CredentialsResponse("john.doe", "password");
        when(userAccountService.initializeNewAccount(any())).thenReturn(credentials);

        CredentialsResponse result = traineeService.createTrainee(trainee);

        assertThat(result).isEqualTo(credentials);
        assertThat(trainee.getRole()).isEqualTo(Role.TRAINEE);
        verify(traineeDao).create(trainee);
    }
}
