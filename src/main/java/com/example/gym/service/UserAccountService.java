package com.example.gym.services;

import com.example.gym.entities.UserEntity;
import com.example.gym.utils.ValidationUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserAccountService {

    private CredentialsService credentialsService;

    @Autowired
    public void setCredentialsService(CredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @Transactional
    public void initializeNewAccount(UserEntity user) {
        ValidationUtility.validateUser(user);

        String username = credentialsService.generateUniqueUsername(
                user.getFirstName(), user.getLastName());
        String password = credentialsService.generatePassword();

        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);
    }

    @Transactional
    public void changePassword(UserEntity user, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        user.setPassword(newPassword);
    }

    @Transactional
    public void activate(UserEntity user, String label) {
        if (user.isActive()) {
            throw new IllegalStateException(label + " is already active");
        }
        user.setActive(true);
    }

    @Transactional
    public void deactivate(UserEntity user, String label) {
        if (!user.isActive()) {
            throw new IllegalStateException(label + " is already inactive");
        }
        user.setActive(false);
    }
}