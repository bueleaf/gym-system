package com.example.training.controller;

import com.example.training.dto.request.TrainerWorkloadEvent;
import com.example.training.dto.response.TrainerMonthlyWorkloadResponse;
import com.example.training.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workloads")
public class TrainerWorkloadController
{
    private final TrainerWorkloadService trainerWorkloadService;

    public TrainerWorkloadController(
            TrainerWorkloadService trainerWorkloadService
    )
    {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerMonthlyWorkloadResponse> getTrainerWorkloads(
            @PathVariable String username,
            @RequestParam Integer month,
            @RequestParam Integer year
    )
    {
        TrainerMonthlyWorkloadResponse response =
                trainerWorkloadService.getMonthlyWorkload(username, month, year);

        return ResponseEntity.ok().body(response);
    }

}
