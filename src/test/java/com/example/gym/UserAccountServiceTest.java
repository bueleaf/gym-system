package com.example.gym;

import com.example.gym.entities.TraineeEntity;
import com.example.gym.services.CredentialsService;
import com.example.gym.services.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock private CredentialsService credentialsService;
    @InjectMocks private UserAccountService userAccountService;

    @Test
    void initializeNewAccount_setsUsernamePasswordAndActive() {
        TraineeEntity u = new TraineeEntity();
        u.setFirstName("John");
        u.setLastName("Doe");

        when(credentialsService.generateUniqueUsername("John","Doe"))
                .thenReturn("John.Doe");
        when(credentialsService.generatePassword()).thenReturn("pass123");

        userAccountService.initializeNewAccount(u);

        assertThat(u.getUsername()).isEqualTo("John.Doe");
        assertThat(u.getPassword()).isEqualTo("pass123");
        assertThat(u.isActive()).isTrue();
    }

    @Test
    void changePassword_rejectsBlank() {
        TraineeEntity u = new TraineeEntity();

        assertThatThrownBy(() -> userAccountService.changePassword(u," "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void activate_nonIdempotent() {
        TraineeEntity u = new TraineeEntity();
        u.setActive(true);

        assertThatThrownBy(() -> userAccountService.activate(u,"x"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deactivate_nonIdempotent() {
        TraineeEntity u = new TraineeEntity();
        u.setActive(false);

        assertThatThrownBy(() -> userAccountService.deactivate(u,"x"))
                .isInstanceOf(IllegalStateException.class);
    }
}