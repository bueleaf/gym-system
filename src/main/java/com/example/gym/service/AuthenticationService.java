package com.example.gym.service;

import com.example.gym.dao.UserDao;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private UserDao userDao;
    private TraineeService traineeService;
    private TrainerService trainerService;

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

    public void changePassword(
            String username,
            String oldPassword,
            String newPassword) {

        UserEntity user = authenticate(username, oldPassword);

        if (user instanceof TraineeEntity) {
            traineeService.changePassword(username, newPassword);
        } else if (user instanceof TrainerEntity) {
            trainerService.changePassword(username, newPassword);
        } else {
            throw new SecurityException("Unsupported user type: " + username);
        }
    }

    @Autowired
    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }
}
