package com.example.gym.controller;

import com.example.gym.dto.request.ActivationRequest;
import com.example.gym.dto.request.TrainerRegistrationRequest;
import com.example.gym.dto.request.UpdateTrainerProfileRequest;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.dto.response.TraineeSummaryResponse;
import com.example.gym.dto.response.TrainerProfileResponse;
import com.example.gym.dto.response.TrainingTypeResponse;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingTypeEntity;
import com.example.gym.application.TrainerManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Api(tags = "Trainers")
public class TrainerController {
    private TrainerManagementService trainerManagementService;

    @ApiOperation("Register a new trainer")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trainer registered successfully"),
            @ApiResponse(code = 400, message = "Invalid registration data"),
            @ApiResponse(code = 404, message = "Training type not found")
    })
    @PostMapping("/registration")
    public ResponseEntity<CredentialsResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        CredentialsResponse response =
                trainerManagementService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @ApiOperation("Get trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile returned successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping
    public ResponseEntity<TrainerProfileResponse> getProfile(
            Authentication authentication
    ) {
        TrainerEntity trainer =
                trainerManagementService.getProfile(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                toProfileResponse(trainer)
        );
    }

    @ApiOperation("Update trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile updated successfully"),
            @ApiResponse(code = 400, message = "Invalid profile data"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @PutMapping
    public ResponseEntity<TrainerProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody
            UpdateTrainerProfileRequest request
    ) {
        TrainerEntity updated =
                trainerManagementService.updateProfile(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(
                toProfileResponse(updated)
        );
    }

    @ApiOperation("Activate or deactivate trainer profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Status changed successfully"),
            @ApiResponse(code = 400, message = "Invalid activation data"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainer not found"),
            @ApiResponse(code = 409, message = "Trainer already has requested status")
    })
    @PatchMapping("/active")
    public ResponseEntity<Void> changeActiveStatus(
            Authentication authentication,
            @Valid @RequestBody ActivationRequest request
    ) {
        trainerManagementService.changeActiveStatus(
                authentication.getName(),
                request.active()
        );

        return ResponseEntity.noContent().build();
    }

    private TrainerProfileResponse toProfileResponse(
            TrainerEntity trainer) {

        List<TraineeSummaryResponse> trainees =
                trainer.getTrainees()
                        .stream()
                        .map(this::toTraineeSummary)
                        .toList();

        return new TrainerProfileResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                toTrainingTypeResponse(
                        trainer.getSpecialization()
                ),
                trainer.isActive(),
                trainees
        );
    }

    private TraineeSummaryResponse toTraineeSummary(
            TraineeEntity trainee) {

        return new TraineeSummaryResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName()
        );
    }

    private TrainingTypeResponse toTrainingTypeResponse(
            TrainingTypeEntity type) {

        return new TrainingTypeResponse(
                type.getId(),
                type.getTrainingTypeName()
        );
    }

    @Autowired
    public void setTrainerManagementService(TrainerManagementService trainerManagementService) {
        this.trainerManagementService = trainerManagementService;
    }
}
