package com.example.gym;

import com.example.gym.daos.UserDao;
import com.example.gym.services.CredentialsService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialsServiceTest {

    @Mock private UserDao userDao;
    @InjectMocks private CredentialsService credentialsService;

    @Test
    void shouldReturnBaseUsernameWhenNotTaken() {
        when(userDao.existsByUsername("John.Doe")).thenReturn(false);
        assertThat(credentialsService.generateUniqueUsername("John","Doe"))
                .isEqualTo("John.Doe");
    }

    @Test
    void shouldAppendNumberWhenTaken() {
        when(userDao.existsByUsername("John.Doe")).thenReturn(true);
        when(userDao.existsByUsername("John.Doe1")).thenReturn(false);

        assertThat(credentialsService.generateUniqueUsername("John","Doe"))
                .isEqualTo("John.Doe1");
    }

    @Test
    void shouldKeepIncrementingUntilUnique() {
        when(userDao.existsByUsername("A.B")).thenReturn(true);
        when(userDao.existsByUsername("A.B1")).thenReturn(true);
        when(userDao.existsByUsername("A.B2")).thenReturn(false);

        assertThat(credentialsService.generateUniqueUsername("A","B"))
                .isEqualTo("A.B2");
    }

    @Test
    void shouldGeneratePasswordLength10() {
        assertThat(credentialsService.generatePassword()).hasSize(10);
    }

    @Test
    void shouldGenerateAlphanumericPassword() {
        assertThat(credentialsService.generatePassword()).matches("[A-Za-z0-9]+");
    }
}