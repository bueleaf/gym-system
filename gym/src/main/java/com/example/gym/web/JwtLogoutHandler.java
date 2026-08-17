package com.example.gym.web;

import com.example.gym.service.RevokedTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtLogoutHandler implements LogoutHandler {
    private final RevokedTokenService revokedTokenService;
    private final JwtDecoder jwtDecoder;
    private final BearerTokenResolver bearerTokenResolver;

    public JwtLogoutHandler(
            RevokedTokenService revokedTokenService,
            JwtDecoder jwtDecoder,
            BearerTokenResolver bearerTokenResolver
    ) {
        this.revokedTokenService = revokedTokenService;
        this.jwtDecoder = jwtDecoder;
        this.bearerTokenResolver = bearerTokenResolver;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String token =
                bearerTokenResolver.resolve(request);

        if (token == null) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);

            revokedTokenService.revoke(
                    jwt.getId(),
                    jwt.getExpiresAt()
            );
        } catch (JwtException exception) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
        }
    }
}