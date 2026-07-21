package com.example.gym.actuator.health;

import com.example.gym.dao.TrainingTypeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeHealthIndicator implements HealthIndicator {
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    public Health health() {
        try
        {
            int count = trainingTypeDao.findAll().size();

            if (count == 0){
                return Health.down()
                        .withDetail("reason", "No training types detected")
                        .build();
            }

            return Health.up()
                    .withDetail("count", count)
                    .build();
        }
        catch (Exception e)
        {
            return Health.down(e).build();
        }
    }
}
