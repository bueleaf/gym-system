package com.example.gym;

import com.example.gym.application.AuthenticationManagementService;
import com.example.gym.application.TraineeManagementService;
import com.example.gym.controller.AuthController;
import com.example.gym.controller.TraineeController;
import com.example.gym.dto.request.LoginRequest;
import com.example.gym.dto.response.LoginResponse;
import com.example.gym.entity.TraineeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllerRequestShapeTest {
    @Test
    void loginReturnsTokenFromAuthenticationManagementService() {
        AuthenticationManagementService service = mock(AuthenticationManagementService.class);
        AuthController controller = new AuthController();
        controller.setAuthenticationManagementService(service);
        LoginRequest request = new LoginRequest("john.doe", "password");
        LoginResponse loginResponse = new LoginResponse("token", "Bearer");
        when(service.login(request)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertThat(response.getBody()).isEqualTo(loginResponse);
        verify(service).login(request);
    }

    @Test
    void traineeProfileUsesAuthenticatedUsername() {
        TraineeManagementService service = mock(TraineeManagementService.class);
        Authentication authentication = mock(Authentication.class);
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUsername("john.doe");
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        when(authentication.getName()).thenReturn("john.doe");
        when(service.getProfile("john.doe")).thenReturn(trainee);

        TraineeController controller = new TraineeController();
        controller.setTraineeManagementService(service);
        ResponseEntity<?> response = controller.getProfile(authentication);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(service).getProfile("john.doe");
    }
}
