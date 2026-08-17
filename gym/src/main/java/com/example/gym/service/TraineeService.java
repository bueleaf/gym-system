package com.example.gym.service;

import com.example.gym.dao.TraineeDao;
import com.example.gym.dao.TrainerDao;
import com.example.gym.dto.request.UpdateTraineeProfileRequest;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.util.ValidationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
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
    public CredentialsResponse createTrainee(TraineeEntity trainee) {
        ValidationUtility.validateUser(trainee);
        CredentialsResponse response =
                userAccountService.initializeNewAccount(trainee);

        traineeDao.create(trainee);
        return response;
    }

    @Transactional
    public TraineeEntity updateOwnProfile(String username, UpdateTraineeProfileRequest request) {
        TraineeEntity trainee = findTrainee(username);

        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(request.active());

        ValidationUtility.validateUser(trainee);
        return traineeDao.update(trainee);
    }

    public TraineeEntity getTraineeByUsername(String username) {
        return findTrainee(username);
    }

    public TraineeEntity getTraineeProfileByUsername(String username) {
        TraineeEntity trainee = findTrainee(username);

        trainee.getTrainers().forEach(
                trainer -> trainer.getSpecialization()
                        .getTrainingTypeName()
        );

        return trainee;
    }

    public List<TraineeEntity> getAllTrainees() {
        return traineeDao.findAll();
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
    public List<TrainerEntity> updateTraineeTrainers(
            String traineeUsername,
            List<String> trainerUsernames) {

        ValidationUtility.validateTrainerUsernames(trainerUsernames);

        TraineeEntity trainee = findTrainee(traineeUsername);

        for (TrainerEntity trainer :
                new HashSet<>(trainee.getTrainers())) {
            trainee.removeTrainer(trainer);
        }

        for (String trainerUsername : trainerUsernames) {
            TrainerEntity trainer = trainerDao
                    .findByUsername(trainerUsername)
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Trainer not found: "
                                            + trainerUsername
                            )
                    );

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
}
