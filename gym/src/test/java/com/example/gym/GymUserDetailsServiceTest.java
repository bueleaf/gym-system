package com.example.gym;

import com.example.gym.dao.UserDao;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.model.Role;
import com.example.gym.service.GymUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymUserDetailsServiceTest {
    @Mock private UserDao userDao;

    @Test
    void loadUserByUsernameUsesPersistedRoleAsAuthority() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUsername("john.doe");
        trainee.setPassword("encoded-password");
        trainee.setActive(true);
        trainee.setRole(Role.TRAINEE);
        when(userDao.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        UserDetails result = new GymUserDetailsService(userDao)
                .loadUserByUsername("john.doe");

        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_TRAINEE");
    }
}
