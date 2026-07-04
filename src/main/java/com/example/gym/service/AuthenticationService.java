package com.example.gym.services;

import com.example.gym.daos.UserDao;
import com.example.gym.entities.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private UserDao userDao;

    public UserEntity authenticate(String username, String password) {
        logger.debug("Attempting authentication for username: {}", username);

        UserEntity user = userDao.findByUsername(username)
                .orElseThrow(() ->
                        new SecurityException("Invalid username or password"));

        if (!password.equals(user.getPassword())) {
            logger.warn("Failed authentication attempt for username: {}", username);
            throw new SecurityException("Invalid username or password");
        }

        logger.debug("Authentication successful for {}", username);
        return user;
    }

    @Autowired
    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }
}