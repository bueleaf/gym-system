package com.example.gym.service;

import com.example.gym.dao.UserDao;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.entity.UserEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserAccountService {
    private final UserDao userDao;
    private final CredentialsService credentialsService;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
            UserDao userDao,
            CredentialsService credentialsService,
            PasswordEncoder passwordEncoder
    ) {
        this.userDao = userDao;
        this.credentialsService = credentialsService;
        this.passwordEncoder = passwordEncoder;
    }

    public CredentialsResponse initializeNewAccount(
            UserEntity user
    ) {
        String username =
                credentialsService.generateUniqueUsername(
                        user.getFirstName(),
                        user.getLastName()
                );

        String rawPassword =
                credentialsService.generatePassword();

        user.setUsername(username);
        user.setPassword(
                passwordEncoder.encode(rawPassword)
        );
        user.setActive(true);
        user.setFailedLoginAttempts(0);
        user.setLockExpiresAt(null);

        return new CredentialsResponse(
                username,
                rawPassword
        );
    }

    @Transactional
    public void changePassword(
            String username,
            String oldPassword,
            String newPassword
    ) {
        UserEntity user =
                findUser(username);

        if (!passwordEncoder.matches(
                oldPassword,
                user.getPassword()
        )) {
            throw new BadCredentialsException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "New password must be different "
                            + "from the current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );
    }

    public void activate(
            UserEntity user,
            String accountName
    ) {
        if (user.isActive()) {
            throw new IllegalStateException(
                    accountName + " is already active"
            );
        }

        user.setActive(true);
    }

    public void deactivate(
            UserEntity user,
            String accountName
    ) {
        if (!user.isActive()) {
            throw new IllegalStateException(
                    accountName + " is already inactive"
            );
        }

        user.setActive(false);
    }

    private UserEntity findUser(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "User not found: " + username
                        )
                );
    }
}
