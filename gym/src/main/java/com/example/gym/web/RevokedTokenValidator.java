package com.example.gym.web;

import com.example.gym.service.RevokedTokenService;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class RevokedTokenValidator
        implements OAuth2TokenValidator<Jwt> {

    private final RevokedTokenService revokedTokenService;

    public RevokedTokenValidator(
            RevokedTokenService revokedTokenService
    ) {
        this.revokedTokenService = revokedTokenService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (revokedTokenService.isRevoked(jwt.getId())) {
            OAuth2Error error =
                    new OAuth2Error(
                            "invalid_token",
                            "Token has been revoked",
                            null
                    );

            return OAuth2TokenValidatorResult.failure(
                    error
            );
        }

        return OAuth2TokenValidatorResult.success();
    }
}