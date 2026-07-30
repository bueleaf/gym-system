package com.example.gym;

import com.example.gym.dao.UserDao;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.service.CredentialsService;
import com.example.gym.service.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {
    @Mock private UserDao userDao;
    @Mock private CredentialsService credentialsService;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserAccountService userAccountService;

    @Test
    void initializeNewAccountStoresEncodedPasswordAndReturnsRawCredentials() {
        TraineeEntity user = new TraineeEntity();
        user.setFirstName("John");
        user.setLastName("Doe");
        when(credentialsService.generateUniqueUsername("John", "Doe"))
                .thenReturn("john.doe");
        when(credentialsService.generatePassword()).thenReturn("raw-password");
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");

        CredentialsResponse response = userAccountService.initializeNewAccount(user);

        assertThat(response).isEqualTo(new CredentialsResponse("john.doe", "raw-password"));
        assertThat(user.getPassword()).isEqualTo("encoded-password");
    }
}
