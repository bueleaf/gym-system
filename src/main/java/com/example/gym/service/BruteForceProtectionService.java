package com.example.gym.service;

import com.example.gym.dao.UserDao;
import com.example.gym.entity.UserEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class BruteForceProtectionService {
    private static final int MAX_ATTEMPTS = 3;

    private static final Duration LOCK_DURATION =
            Duration.ofMinutes(5);

    private final UserDao userDao;
    private final Clock clock;

    public BruteForceProtectionService(
            UserDao userDao,
            Clock clock
    ) {
        this.userDao = userDao;
        this.clock = clock;
    }

    @Transactional
    public void ensureNotBlocked(String username) {
        UserEntity user =
                findUserSilently(username);

        if (user == null) {
            return;
        }

        Instant lockExpiresAt =
                user.getLockExpiresAt();

        if (lockExpiresAt == null) {
            return;
        }

        Instant now = clock.instant();

        if (now.isBefore(lockExpiresAt)) {
            throw new LockedException(
                    "User is temporarily locked"
            );
        }

        user.setFailedLoginAttempts(0);
        user.setLockExpiresAt(null);
    }

    @Transactional
    public boolean loginFailed(String username) {
        UserEntity user =
                findUserSilently(username);

        if (user == null) {
            return false;
        }

        int attempts =
                user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_ATTEMPTS) {
            user.setLockExpiresAt(
                    clock.instant()
                            .plus(LOCK_DURATION)
            );

            return true;
        }

        return false;
    }

    @Transactional
    public void loginSucceeded(String username) {
        UserEntity user =
                findUserSilently(username);

        if (user == null) {
            return;
        }

        user.setFailedLoginAttempts(0);
        user.setLockExpiresAt(null);
    }

    private UserEntity findUserSilently(
            String username
    ) {
        if (username == null || username.isBlank()) {
            return null;
        }

        return userDao.findByUsername(
                username.trim()
        ).orElse(null);
    }
}