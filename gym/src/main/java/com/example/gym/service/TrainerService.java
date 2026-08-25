package com.example.gym.service;

import com.example.gym.dao.TrainerDao;
import com.example.gym.dto.request.UpdateTrainerProfileRequest;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.model.Role;
import com.example.gym.util.ValidationUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrainerService {
    private TrainerDao trainerDao;
    private UserAccountService userAccountService;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUserAccountService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Transactional
    public CredentialsResponse createTrainer(TrainerEntity trainer) {
        ValidationUtility.validateTrainer(trainer);
        trainer.setRole(Role.TRAINER);
        CredentialsResponse response =
                userAccountService.initializeNewAccount(trainer);

        trainerDao.create(trainer);
        return response;
    }

    @Transactional
    public TrainerEntity updateOwnProfile(String username, UpdateTrainerProfileRequest request) {
        TrainerEntity trainer = findTrainer(username);

        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setActive(request.active());

        ValidationUtility.validateTrainer(trainer);
        return trainerDao.update(trainer);
    }

    public TrainerEntity getTrainerByUsername(String username) {
        return findTrainer(username);
    }

    public TrainerEntity getTrainerProfileByUsername(String username) {
        TrainerEntity trainer = findTrainer(username);
        trainer.getTrainees().size();
        return trainer;
    }

    public List<TrainerEntity> getAllTrainers() {
        return trainerDao.findAll();
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

    private TrainerEntity findTrainer(String username) {
        return trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
    }
}
