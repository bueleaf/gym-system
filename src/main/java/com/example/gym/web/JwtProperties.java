package com.example.gym.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        Resource publicKey,
        Resource privateKey,
        Duration expiration,
        String issuer
) {
}