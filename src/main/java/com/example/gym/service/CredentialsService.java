package com.example.gym.services;

import com.example.gym.daos.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class CredentialsService {
    private static final Logger logger = LoggerFactory.getLogger(CredentialsService.class);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;

    private UserDao userDao;

    public String generateUniqueUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;

        if (!usernameExists(baseUsername)) {
            return baseUsername;
        }

        int serialNumber = 1;
        String candidate;
        do {
            candidate = baseUsername + serialNumber;
            serialNumber++;
        } while (usernameExists(candidate));

        logger.debug("Generated unique username: {}", candidate);
        return candidate;
    }

    public String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    private boolean usernameExists(String username) {
        return userDao.existsByUsername(username);
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}