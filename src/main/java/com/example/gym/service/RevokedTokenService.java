package com.example.gym.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RevokedTokenService {
    private final Map<String, Instant> revokedTokens =
            new ConcurrentHashMap<>();

    private final Clock clock;

    public RevokedTokenService(Clock clock) {
        this.clock = clock;
    }

    public void revoke(
            String tokenId,
            Instant expiresAt
    ) {
        if (tokenId == null || expiresAt == null) {
            return;
        }

        removeExpiredTokens();

        revokedTokens.put(
                tokenId,
                expiresAt
        );
    }

    public boolean isRevoked(String tokenId) {
        if (tokenId == null) {
            return false;
        }

        Instant expiresAt =
                revokedTokens.get(tokenId);

        if (expiresAt == null) {
            return false;
        }

        if (!clock.instant().isBefore(expiresAt)) {
            revokedTokens.remove(tokenId);
            return false;
        }

        return true;
    }

    private void removeExpiredTokens() {
        Instant now = clock.instant();

        revokedTokens.entrySet()
                .removeIf(entry ->
                        !now.isBefore(entry.getValue())
                );
    }
}