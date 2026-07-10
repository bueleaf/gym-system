package com.example.gym.facade;

import com.example.gym.dto.request.AddTrainingRequest;
import com.example.gym.dto.request.TraineeTrainingSearchCriteria;
import com.example.gym.dto.request.TrainerTrainingSearchCriteria;
import com.example.gym.dto.request.UpdateTraineeProfileRequest;
import com.example.gym.dto.request.UpdateTrainerProfileRequest;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.entity.TrainingTypeEntity;
import com.example.gym.entity.UserEntity;
import com.example.gym.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GymFacade {
    private static final Logger logger = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final AuthenticationService authenticationService;

    @Autowired
    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     TrainingTypeService trainingTypeService,
                     AuthenticationService authenticationService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.authenticationService = authenticationService;
    }

    public TrainerEntity createTrainerProfile(String firstName, String lastName,
                                              String trainingTypeName) {
        logger.info("Creating trainer profile: {} {} with specialization: {}",
                firstName, lastName, trainingTypeName);

        TrainingTypeEntity specialization =
                trainingTypeService.getTrainingTypeByName(trainingTypeName);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);

        return trainerService.createTrainer(trainer);
    }

    public TraineeEntity createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        logger.info("Creating trainee profile: {} {}", firstName, lastName);

        TraineeEntity trainee = new TraineeEntity();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        TraineeEntity createdTrainee = traineeService.createTrainee(trainee);
        logger.info("Successfully created trainee profile with username: {}", createdTrainee.getUsername());
        return createdTrainee;
    }

    public UserEntity authenticateUser(String username, String password) {
        logger.debug("Authenticating user: {}", username);
        return authenticationService.authenticate(username, password);
    }

    public TrainerEntity getTrainerByUsername(String username, String password) {
        logger.debug("Getting trainer profile for username: {}", username);
        authenticateAndValidateTrainer(username, password);

        return trainerService.getTrainerProfileByUsername(username);
    }

    public TraineeEntity getTraineeByUsername(String username, String password) {
        logger.debug("Getting trainee profile for username: {}", username);
        authenticateAndValidateTrainee(username, password);

        return traineeService.getTraineeProfileByUsername(username);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        logger.info("Changing password for trainee: {}", username);
        authenticateAndValidateTrainee(username, oldPassword);

        traineeService.changePassword(username, newPassword);
        logger.info("Successfully changed password for trainee: {}", username);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        logger.info("Changing password for trainer: {}", username);
        authenticateAndValidateTrainer(username, oldPassword);

        trainerService.changePassword(username, newPassword);
        logger.info("Successfully changed password for trainer: {}", username);
    }

    public TrainerEntity updateTrainerProfile(String username, String password, UpdateTrainerProfileRequest request) {
        logger.info("Updating trainer profile for username: {}", username);
        authenticateAndValidateTrainer(username, password);

        trainerService.updateOwnProfile(username, request);
        TrainerEntity result =
                trainerService.getTrainerProfileByUsername(username);
        logger.info("Successfully updated trainer profile for username: {}", username);
        return result;
    }

    public TraineeEntity updateTraineeProfile(String username, String password, UpdateTraineeProfileRequest request) {
        logger.info("Updating trainee profile for username: {}", username);
        authenticateAndValidateTrainee(username, password);

        traineeService.updateOwnProfile(username, request);
        TraineeEntity result =
                traineeService.getTraineeProfileByUsername(username);
        logger.info("Successfully updated trainee profile for username: {}", username);
        return result;
    }

    public void activateTrainee(String username, String password) {
        logger.info("Activating trainee: {}", username);
        authenticateAndValidateTrainee(username, password);
        traineeService.activateTrainee(username);
    }

    public void deactivateTrainee(String username, String password) {
        logger.info("Deactivating trainee: {}", username);
        authenticateAndValidateTrainee(username, password);
        traineeService.deactivateTrainee(username);
    }

    public void activateTrainer(String username, String password) {
        logger.info("Activating trainer: {}", username);
        authenticateAndValidateTrainer(username, password);
        trainerService.activateTrainer(username);
    }

    public void deactivateTrainer(String username, String password) {
        logger.info("Deactivating trainer: {}", username);
        authenticateAndValidateTrainer(username, password);
        trainerService.deactivateTrainer(username);
    }

    public void deleteTraineeProfile(String username, String password) {
        logger.info("Deleting trainee profile: {}", username);
        authenticateAndValidateTrainee(username, password);
        traineeService.deleteTraineeByUsername(username);
        logger.info("Successfully deleted trainee profile: {}", username);
    }

    public List<TrainingEntity> getTraineeTrainings(
            String username,
            String password,
            TraineeTrainingSearchCriteria criteria) {

        logger.debug("Getting trainings for trainee: {} with criteria", username);

        authenticateAndValidateTrainee(username, password);

        return trainingService.getTraineeTrainingsByCriteria(
                username,
                criteria);
    }


    public List<TrainingEntity> getTrainerTrainings(
            String username,
            String password,
            TrainerTrainingSearchCriteria criteria) {

        logger.debug("Getting trainings for trainer: {} with criteria", username);

        authenticateAndValidateTrainer(username, password);

        return trainingService.getTrainerTrainingsByCriteria(
                username,
                criteria);
    }

    public List<TrainingTypeEntity> getTrainingTypes(
            String username,
            String password) {
        logger.debug("Getting training types for user: {}", username);

        authenticateUser(username, password);

        return trainingTypeService.getAllTrainingTypes();
    }

    public void changePassword(
            String username,
            String oldPassword,
            String newPassword) {
        UserEntity user = authenticateUser(username, oldPassword);

        if (user instanceof TraineeEntity) {
            traineeService.changePassword(username, newPassword);
        } else if (user instanceof TrainerEntity) {
            trainerService.changePassword(username, newPassword);
        } else {
            throw new SecurityException(
                    "Unsupported user type: " + username
            );
        }
    }

    public TrainingEntity addTraining(String username, String password, AddTrainingRequest request) {
        logger.info("Adding training: {} by user: {}", request.trainingName(), username);
        authenticateUser(username, password);

        TraineeEntity trainee = traineeService.getTraineeByUsername(
                request.traineeUsername()
        );

        TrainerEntity trainer = trainerService.getTrainerByUsername(
                request.trainerUsername()
        );

        TrainingEntity training = new TrainingEntity();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(request.trainingName());
        training.setTrainingDate(request.trainingDate());
        training.setTrainingDuration(request.trainingDuration());
        training.setTrainingType(trainer.getSpecialization());

        TrainingEntity result = trainingService.createTraining(training);
        logger.info("Successfully added training with ID: {}", result.getId());
        return result;
    }

    public List<TrainerEntity> getUnassignedTrainers(String traineeUsername, String password) {
        logger.debug("Getting unassigned trainers for trainee: {}", traineeUsername);
        authenticateAndValidateTrainee(traineeUsername, password);

        return traineeService.getUnassignedTrainers(traineeUsername);
    }

    public List<TrainerEntity> updateTraineeTrainers(String traineeUsername, String password, List<String> trainerUsernames) {
        logger.info("Updating trainers list for trainee: {}", traineeUsername);
        authenticateAndValidateTrainee(traineeUsername, password);

        List<TrainerEntity> result = traineeService.updateTraineeTrainers(traineeUsername, trainerUsernames);
        logger.info("Successfully updated trainers list for trainee: {}", traineeUsername);
        return result;
    }

    private UserEntity authenticateAndValidateTrainee(String username, String password) {
        UserEntity user = authenticateUser(username, password);
        if (!(user instanceof TraineeEntity)) {
            throw new SecurityException("User is not a trainee: " + username);
        }

        return user;
    }

    private UserEntity authenticateAndValidateTrainer(String username, String password) {
        UserEntity user = authenticateUser(username, password);
        if (!(user instanceof TrainerEntity)) {
            throw new SecurityException("User is not a trainer: " + username);
        }

        return user;
    }
}
