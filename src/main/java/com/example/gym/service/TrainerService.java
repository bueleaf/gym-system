package com.example.gym.services;

import com.example.gym.daos.TrainerDao;
import com.example.gym.daos.TrainingTypeDao;
import com.example.gym.dtos.UpdateTrainerProfileRequest;
import com.example.gym.entities.TrainerEntity;
import com.example.gym.entities.TrainingTypeEntity;
import com.example.gym.utils.ValidationUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TrainerService {

    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private UserAccountService userAccountService;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setUserAccountService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Transactional
    public TrainerEntity createTrainer(TrainerEntity trainer) {
        validateTrainer(trainer);
        userAccountService.initializeNewAccount(trainer);
        trainerDao.create(trainer);
        return trainer;
    }

    @Transactional
    public TrainerEntity updateOwnProfile(String username, UpdateTrainerProfileRequest request) {
        TrainerEntity trainer = findTrainer(username);

        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(request.getLastName());

        TrainingTypeEntity specialization = trainingTypeDao.findById(request.getSpecializationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + request.getSpecializationId()));
        trainer.setSpecialization(specialization);

        validateTrainer(trainer);
        return trainerDao.update(trainer);
    }

    public TrainerEntity getTrainerByUsername(String username) {
        return findTrainer(username);
    }

    public List<TrainerEntity> getAllTrainers() {
        return trainerDao.findAll();
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        TrainerEntity trainer = findTrainer(username);
        userAccountService.changePassword(trainer, newPassword);
        trainerDao.update(trainer);
    }

    @Transactional
    public void activateTrainer(String username) {
        TrainerEntity trainer = findTrainer(username);
        userAccountService.activate(trainer, "Trainer " + username);
        trainerDao.update(trainer);
    }

    @Transactional
    public void deactivateTrainer(String username) {
        TrainerEntity trainer = findTrainer(username);
        userAccountService.deactivate(trainer, "Trainer " + username);
        trainerDao.update(trainer);
    }

    public Optional<TrainingTypeEntity> getTrainingTypeByName(String name) {
        return trainingTypeDao.findByName(name);
    }

    private void validateTrainer(TrainerEntity trainer) {
        ValidationUtility.validateUser(trainer);
        if (trainer.getSpecialization() == null) {
            throw new IllegalArgumentException("Specialization is required");
        }
    }

    private TrainerEntity findTrainer(String username) {
        return trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
    }
}