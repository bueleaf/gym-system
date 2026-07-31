package com.example.gym.application;

import com.example.gym.dto.request.ChangePasswordRequest;
import com.example.gym.dto.request.LoginRequest;
import com.example.gym.dto.response.LoginResponse;
import com.example.gym.service.BruteForceProtectionService;
import com.example.gym.service.JwtService;
import com.example.gym.service.UserAccountService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationManagementService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final UserAccountService userAccountService;

    public AuthenticationManagementService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            BruteForceProtectionService bruteForceProtectionService,
            UserAccountService userAccountService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.bruteForceProtectionService =
                bruteForceProtectionService;
        this.userAccountService = userAccountService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();

        bruteForceProtectionService.ensureNotBlocked(
                username
        );

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken
                                    .unauthenticated(
                                            username,
                                            request.password()
                                    )
                    );

            bruteForceProtectionService.loginSucceeded(
                    username
            );

            return new LoginResponse(
                    jwtService.generateToken(authentication),
                    "Bearer"
            );
        } catch (BadCredentialsException exception) {
            boolean locked =
                    bruteForceProtectionService.loginFailed(
                            username
                    );

            if (locked) {
                throw new LockedException(
                        "User is temporarily locked for 5 minutes"
                );
            }

            throw exception;
        }
    }

    public void changePassword(
            String authenticatedUsername,
            ChangePasswordRequest request
    ) {
        userAccountService.changePassword(
                authenticatedUsername,
                request.oldPassword(),
                request.newPassword()
        );
    }
}