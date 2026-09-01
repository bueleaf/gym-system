package com.example.gym.service;

import com.example.gym.application.AuthenticationManagementService;
import com.example.gym.dto.request.LoginRequest;
import com.example.gym.dto.response.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationManagementServiceTest {
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private BruteForceProtectionService bruteForceProtectionService;
    @Mock private UserAccountService userAccountService;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthenticationManagementService authenticationManagementService;

    @Test
    void loginReturnsBearerTokenForValidCredentials() {
        LoginRequest request = new LoginRequest("john.doe", "password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn("jwt-token");

        LoginResponse response = authenticationManagementService.login(request);

        assertThat(response).isEqualTo(new LoginResponse("jwt-token", "Bearer"));
        verify(bruteForceProtectionService).ensureNotBlocked("john.doe");
        verify(bruteForceProtectionService).loginSucceeded("john.doe");
    }

    @Test
    void loginRecordsFailedAttemptForInvalidCredentials() {
        LoginRequest request = new LoginRequest("john.doe", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("invalid"));

        assertThatThrownBy(() -> authenticationManagementService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(bruteForceProtectionService).loginFailed("john.doe");
        verify(bruteForceProtectionService, never()).loginSucceeded(anyString());
    }
}
