package com.example.gym.actuator.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsWrapper {
    private Counter traineeRegistrations;
    private Counter trainerRegistrations;
    private Counter trainingCreations;

    public MetricsWrapper(MeterRegistry registry) {
        this.traineeRegistrations = Counter.builder("gym.user.registrations")
                .description("Number of trainee registrations")
                .tag("user.type", "trainee")
                .register(registry);

        this.trainerRegistrations = Counter.builder("gym.user.registrations")
                .description("Number of trainer registrations")
                .tag("user.type", "trainer")
                .register(registry);

        this.trainingCreations = Counter.builder("gym.training.creations")
                .description("Number of training creations")
                .register(registry);
    }

    public void recordTraineeRegistered() {
        this.traineeRegistrations.increment();
    }

    public void recordTrainerRegistered() {
        this.trainerRegistrations.increment();
    }

    public void recordTrainingCreated() {
        this.trainingCreations.increment();
    }
}
