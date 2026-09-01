package com.example.gym.metrics;

import com.example.gym.actuator.metric.MetricsWrapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsWrapperTest {

    @Test
    void recordMethodsIncrementTheirCorrespondingCounters() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MetricsWrapper metrics = new MetricsWrapper(registry);

        metrics.recordTraineeRegistered();
        metrics.recordTrainerRegistered();
        metrics.recordTrainingCreated();

        assertThat(registry.get("gym.user.registrations")
                .tag("user.type", "trainee")
                .counter()
                .count()).isEqualTo(1.0);
        assertThat(registry.get("gym.user.registrations")
                .tag("user.type", "trainer")
                .counter()
                .count()).isEqualTo(1.0);
        assertThat(registry.get("gym.training.creations")
                .counter()
                .count()).isEqualTo(1.0);
    }
}
