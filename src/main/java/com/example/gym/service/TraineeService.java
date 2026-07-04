package com.example.gym.services;

import com.example.gym.daos.TraineeDao;
import com.example.gym.daos.TrainerDao;
import com.example.gym.dtos.UpdateTraineeProfileRequest;
import com.example.gym.entities.TraineeEntity;
import com.example.gym.entities.TrainerEntity;
import com.example.gym.utils.ValidationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private UserAccountService userAccountService;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUserAccountService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Transactional
    public TraineeEntity createTrainee(TraineeEntity trainee) {
        validateTrainee(trainee);
        userAccountService.initializeNewAccount(trainee);
        traineeDao.create(trainee);
        return trainee;
    }

    @Transactional
    public TraineeEntity updateOwnProfile(String username, UpdateTraineeProfileRequest request) {
        TraineeEntity trainee = findTrainee(username);

        trainee.setFirstName(request.getFirstName());
        trainee.setLastName(request.getLastName());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        validateTrainee(trainee);
        return traineeDao.update(trainee);
    }

    public TraineeEntity getTraineeByUsername(String username) {
        return findTrainee(username);
    }

    public List<TraineeEntity> getAllTrainees() {
        return traineeDao.findAll();
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        TraineeEntity trainee = findTrainee(username);
        userAccountService.changePassword(trainee, newPassword);
        traineeDao.update(trainee);
    }

    @Transactional
    public void activateTrainee(String username) {
        TraineeEntity trainee = findTrainee(username);
        userAccountService.activate(trainee, "Trainee " + username);
        traineeDao.update(trainee);
    }

    @Transactional
    public void deactivateTrainee(String username) {
        TraineeEntity trainee = findTrainee(username);
        userAccountService.deactivate(trainee, "Trainee " + username);
        traineeDao.update(trainee);
    }

    @Transactional
    public void deleteTraineeByUsername(String username) {
        TraineeEntity trainee = findTrainee(username);
        traineeDao.delete(trainee.getId());
    }

    @Transactional
    public List<TrainerEntity> updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        TraineeEntity trainee = findTrainee(traineeUsername);

        for (TrainerEntity trainer : new HashSet<>(trainee.getTrainers())) {
            trainee.removeTrainer(trainer);
        }

        List<String> usernames = trainerUsernames == null ? List.of() : trainerUsernames;
        for (String trainerUsername : usernames) {
            TrainerEntity trainer = trainerDao.findByUsername(trainerUsername)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerUsername));
            trainee.addTrainer(trainer);
        }

        traineeDao.update(trainee);
        return new ArrayList<>(trainee.getTrainers());
    }

    public List<TrainerEntity> getUnassignedTrainers(String traineeUsername) {
        TraineeEntity trainee = findTrainee(traineeUsername);
        return trainerDao.findAll().stream()
                .filter(TrainerEntity::isActive)
                .filter(trainer -> !trainee.getTrainers().contains(trainer))
                .collect(Collectors.toList());
    }

    private TraineeEntity findTrainee(String username) {
        return traineeDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
    }

    private void validateTrainee(TraineeEntity trainee) {
        ValidationUtility.validateTraineeProfile(trainee);
    }
}
