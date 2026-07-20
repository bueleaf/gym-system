package com.example.gym.controller;

import com.example.gym.dto.request.ChangePasswordRequest;
import com.example.gym.facade.GymFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/login")
@Api(tags = "Authentication")
public class AuthController {
    private GymFacade gymFacade;

    @ApiOperation("Authenticate user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Authentication successful"),
            @ApiResponse(code = 401, message = "Authentication failed")
    })
    @GetMapping
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        gymFacade.authenticateUser(
                username,
                password
        );

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Change authenticated user's password")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password changed successfully"),
            @ApiResponse(code = 400, message = "Invalid new password"),
            @ApiResponse(code = 401, message = "Authentication failed")
    })
    @PutMapping
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody
            ChangePasswordRequest request) {

        gymFacade.changePassword(
                request.username(),
                request.oldPassword(),
                request.newPassword()
        );

        return ResponseEntity.ok().build();
    }

    @Autowired
    public void getGymFacade(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }
}
