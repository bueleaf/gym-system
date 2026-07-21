package com.example.gym.actuator.health;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GymSchemaHealthIndicator implements HealthIndicator {
    private EntityManager entityManager;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Health health() {
        try
        {
            Long userCount = entityManager.createQuery(
                    "select count(u) from UserEntity u",
                    Long.class
            ).getSingleResult();

            Long trainingCount = entityManager.createQuery(
                    "select count(t) from TrainingEntity t",
                    Long.class
            ).getSingleResult();

            return Health.up()
                    .withDetail("userTableUp", true)
                    .withDetail("trainingTableUp", true)
                    .withDetail("userCount", userCount)
                    .withDetail("trainingCount", trainingCount)
                    .build();
        }
        catch (Exception e)
        {
            return Health.down()
                    .withDetail("reason", "Gym entities couldn't be queried")
                    .withException(e)
                    .build();
        }
    }
}
