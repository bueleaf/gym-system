package com.example.gym.service;

import com.example.gym.dao.UserDao;
import com.example.gym.entity.TraineeEntity;
import com.example.gym.entity.TrainerEntity;
import com.example.gym.entity.UserEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GymUserDetailsService implements UserDetailsService {
    private final UserDao userDao;

    public GymUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        UserEntity user = userDao.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid username or password"
                        )
                );

        String role;

        if (user instanceof TraineeEntity) {
            role = "TRAINEE";
        } else if (user instanceof TrainerEntity) {
            role = "TRAINER";
        } else {
            throw new UsernameNotFoundException(
                    "Invalid username or password"
            );
        }

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(role)
                .disabled(!user.isActive())
                .build();
    }
}