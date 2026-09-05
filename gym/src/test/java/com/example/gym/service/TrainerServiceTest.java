package com.example.gym.service;

import com.example.gym.dao.TrainerDao;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingTypeEntity;
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
class TrainerServiceTest {
    @Mock private TrainerDao trainerDao;
    @Mock private UserAccountService userAccountService;
    @InjectMocks private TrainerService trainerService;

    @Test
    void createTrainerPersistsAccountAndReturnsCredentials() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setFirstName("Mike");
        trainer.setLastName("Smith");
        trainer.setSpecialization(new TrainingTypeEntity("Yoga"));
        CredentialsResponse credentials = new CredentialsResponse("mike.smith", "password");
        when(userAccountService.initializeNewAccount(any())).thenReturn(credentials);

        CredentialsResponse result = trainerService.createTrainer(trainer);

        assertThat(result).isEqualTo(credentials);
        assertThat(trainer.getRole()).isEqualTo(Role.TRAINER);
        verify(trainerDao).create(trainer);
    }
}
