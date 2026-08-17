package com.example.gym.controller;

import com.example.gym.application.AuthenticationManagementService;
import com.example.gym.dto.request.ChangePasswordRequest;
import com.example.gym.dto.request.LoginRequest;
import com.example.gym.dto.response.LoginResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/login")
@Api(tags = "Authentication")
public class AuthController {
    private AuthenticationManagementService authenticationManagementService;

    @ApiOperation("Authenticate user")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Authentication successful"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Authentication failed"
            )
    })
    @PostMapping
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationManagementService.login(request));
    }

    @ApiOperation("Change authenticated user's password")
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "Password changed successfully"
            ),
            @ApiResponse(
                    code = 400,
                    message = "Invalid new password"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Authentication failed"
            )
    })
    @PutMapping
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody
            ChangePasswordRequest request
    ) {
        authenticationManagementService.changePassword(
                authentication.getName(),
                request
        );

        return ResponseEntity.noContent().build();
    }

    @Autowired
    public void setAuthenticationManagementService(
            AuthenticationManagementService authenticationManagementService) {
        this.authenticationManagementService = authenticationManagementService;
    }
}
