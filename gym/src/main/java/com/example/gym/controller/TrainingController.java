package com.example.gym.controller;

import com.example.gym.dto.request.AddTrainingRequest;
import com.example.gym.dto.TraineeTrainingSearchCriteria;
import com.example.gym.dto.TrainerTrainingSearchCriteria;
import com.example.gym.dto.response.TraineeTrainingResponse;
import com.example.gym.dto.response.TrainerTrainingResponse;
import com.example.gym.dto.response.TrainingTypeResponse;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.entity.TrainingTypeEntity;
import com.example.gym.application.TrainingManagementService;
import com.example.gym.util.ValidationUtility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@Api(tags = "Trainings")
public class TrainingController {
    private TrainingManagementService trainingManagementService;

    @ApiOperation("Get trainee trainings using optional search criteria")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainings returned successfully"),
            @ApiResponse(code = 400, message = "Invalid search criteria"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/trainee-trainings")
    public ResponseEntity<List<TraineeTrainingResponse>>
    getTraineeTrainings(
            Authentication authentication,

            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(name = "trainerName", required = false)
            String trainerName,

            @RequestParam(name = "trainingTypeName", required = false)
            String trainingTypeName
    ) {
        ValidationUtility.validateDateRange(
                fromDate,
                toDate
        );

        TraineeTrainingSearchCriteria criteria =
                new TraineeTrainingSearchCriteria(
                        fromDate,
                        toDate,
                        trainerName,
                        trainingTypeName
                );

        List<TraineeTrainingResponse> response =
                trainingManagementService
                        .getTraineeTrainings(
                                authentication.getName(),
                                criteria
                        )
                        .stream()
                        .map(this::toTraineeTrainingResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @ApiOperation("Get trainer trainings using optional search criteria")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainings returned successfully"),
            @ApiResponse(code = 400, message = "Invalid search criteria"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping("/trainer-trainings")
    public ResponseEntity<List<TrainerTrainingResponse>>
    getTrainerTrainings(
            Authentication authentication,

            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(name = "traineeName", required = false)
            String traineeName
    ) {
        ValidationUtility.validateDateRange(
                fromDate,
                toDate
        );

        TrainerTrainingSearchCriteria criteria =
                new TrainerTrainingSearchCriteria(
                        fromDate,
                        toDate,
                        traineeName
                );

        List<TrainerTrainingResponse> response =
                trainingManagementService
                        .getTrainerTrainings(
                                authentication.getName(),
                                criteria
                        )
                        .stream()
                        .map(this::toTrainerTrainingResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @ApiOperation("Add a new training")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training added successfully"),
            @ApiResponse(code = 400, message = "Invalid training data"),
            @ApiResponse(code = 401, message = "Authentication failed"),
            @ApiResponse(code = 404, message = "Trainee or trainer not found")
    })
    @PostMapping("/trainings")
    public ResponseEntity<Void> addTraining(
            Authentication authentication,
            @Valid @RequestBody AddTrainingRequest request
    ) {
        trainingManagementService.addTraining(
                authentication.getName(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/trainings/{trainingId}")
    public ResponseEntity<Void> deleteTraining(
            Authentication authentication,
            @PathVariable Long trainingId
    ) {
        trainingManagementService.deleteTraining(
                authentication.getName(),
                trainingId
        );

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    private TrainerTrainingResponse toTrainerTrainingResponse(
            TrainingEntity training) {

        return new TrainerTrainingResponse(
                training.getId(),
                training.getTrainingName(),
                training.getTrainingDate(),
                toTrainingTypeResponse(training.getTrainingType()),
                training.getTrainingDuration(),
                fullName(
                        training.getTrainee().getFirstName(),
                        training.getTrainee().getLastName()
                )
        );
    }

    private TraineeTrainingResponse toTraineeTrainingResponse(
            TrainingEntity training) {

        return new TraineeTrainingResponse(
                training.getId(),
                training.getTrainingName(),
                training.getTrainingDate(),
                toTrainingTypeResponse(training.getTrainingType()),
                training.getTrainingDuration(),
                fullName(
                        training.getTrainer().getFirstName(),
                        training.getTrainer().getLastName()
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

    private String fullName(
            String firstName,
            String lastName) {

        return firstName + " " + lastName;
    }

    @Autowired
    public void setTrainingManagementService(TrainingManagementService trainingManagementService) {
        this.trainingManagementService = trainingManagementService;
    }
}
