package com.example.gym.controller;

import com.example.gym.dto.response.TrainingTypeResponse;
import com.example.gym.application.TrainingManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Api(tags = "Training Types")
public class TrainingTypeController {

    private TrainingManagementService trainingManagementService;

    @ApiOperation("Get all available training types")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training types returned successfully"),
            @ApiResponse(code = 401, message = "Authentication failed")
    })
    @GetMapping
    public ResponseEntity<List<TrainingTypeResponse>> getAll(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        List<TrainingTypeResponse> response =
                trainingManagementService.getTrainingTypes(
                                username,
                                password
                        )
                        .stream()
                        .map(type -> new TrainingTypeResponse(
                                type.getId(),
                                type.getTrainingTypeName()
                        ))
                        .toList();

        return ResponseEntity.ok(response);
    }

    @Autowired
    public void setTrainingManagementService(TrainingManagementService trainingManagementService) {
        this.trainingManagementService = trainingManagementService;
    }
}
