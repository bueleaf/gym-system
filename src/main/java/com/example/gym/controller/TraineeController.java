package com.example.gym.controller;

import com.example.gym.dto.request.ActivationRequest;
import com.example.gym.dto.request.TraineeRegistrationRequest;
import com.example.gym.dto.request.UpdateTraineeProfileRequest;
import com.example.gym.dto.request.UpdateTraineeTrainersRequest;
import com.example.gym.dto.response.CredentialsResponse;
import com.example.gym.dto.response.TraineeProfileResponse;
import com.example.gym.dto.response.TrainerSummaryResponse;
import com.example.gym.dto.response.TrainingTypeResponse;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.TrainingTypeEntity;
import com.example.gym.facade.GymFacade;
import com.example.gym.util.ValidationUtility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Api(tags = "Trainees")
public class TraineeController {
    private GymFacade gymFacade;

    @ApiOperation("Register a new trainee")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Trainee registered successfully"),
            @ApiResponse(code = 400, message = "Invalid registration data")
    })
    @PostMapping("/registration")
    public ResponseEntity<CredentialsResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request) {

        TraineeEntity created =
                gymFacade.createTraineeProfile(
                        request.firstName(),
                        request.lastName(),
                        request.dateOfBirth(),
                        request.address()
                );

        CredentialsResponse response =
                new CredentialsResponse(
                        created.getUsername(),
                        created.getPassword()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @ApiOperation("Get trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile returned successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        TraineeEntity trainee =
                gymFacade.getTraineeByUsername(
                        username,
                        password
                );

        return ResponseEntity.ok(
                toProfileResponse(trainee)
        );
    }

    @ApiOperation("Update trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile updated successfully"),
            @ApiResponse(code = 400, message = "Invalid profile data"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @PutMapping
    public ResponseEntity<TraineeProfileResponse> updateProfile(
            @Valid @RequestBody UpdateTraineeProfileRequest request) {

        TraineeEntity updated =
                gymFacade.updateTraineeProfile(
                        request.username(),
                        request.password(),
                        request
                );

        return ResponseEntity.ok(
                toProfileResponse(updated)
        );
    }

    @ApiOperation("Delete trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee deleted successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        gymFacade.deleteTraineeProfile(
                username,
                password
        );

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Activate or deactivate trainee profile")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Status changed successfully"),
            @ApiResponse(code = 400, message = "Invalid activation data"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found"),
            @ApiResponse(code = 409, message = "Trainee already has requested status")
    })
    @PatchMapping("/active")
    public ResponseEntity<Void> changeActiveStatus(
            @Valid @RequestBody ActivationRequest request) {

        if (request.active()) {
            gymFacade.activateTrainee(
                    request.username(),
                    request.password()
            );
        } else {
            gymFacade.deactivateTrainee(
                    request.username(),
                    request.password()
            );
        }

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get active trainers not assigned to the trainee")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainers returned successfully"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryResponse>>
    getUnassignedTrainers(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        List<TrainerSummaryResponse> response =
                gymFacade.getUnassignedTrainers(
                                username,
                                password
                        )
                        .stream()
                        .map(this::toTrainerSummary)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @ApiOperation("Replace trainee trainer list")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainer list updated successfully"),
            @ApiResponse(code = 400, message = "Invalid trainer list"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee or trainer not found")
    })
    @PutMapping("/trainers")
    public ResponseEntity<List<TrainerSummaryResponse>>
    updateTrainerList(
            @Valid @RequestBody
            UpdateTraineeTrainersRequest request) {

        ValidationUtility.validateTrainerUsernames(
                request.trainerUsernames()
        );

        List<TrainerSummaryResponse> response =
                gymFacade.updateTraineeTrainers(
                                request.traineeUsername(),
                                request.password(),
                                request.trainerUsernames()
                        )
                        .stream()
                        .map(this::toTrainerSummary)
                        .toList();

        return ResponseEntity.ok(response);
    }

    private TraineeProfileResponse toProfileResponse(
            TraineeEntity trainee) {

        List<TrainerSummaryResponse> trainers =
                trainee.getTrainers()
                        .stream()
                        .map(this::toTrainerSummary)
                        .toList();

        return new TraineeProfileResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.isActive(),
                trainers
        );
    }

    private TrainerSummaryResponse toTrainerSummary(
            TrainerEntity trainer) {

        return new TrainerSummaryResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                toTrainingTypeResponse(
                        trainer.getSpecialization()
                )
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
    public void setGymFacade(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }
}
