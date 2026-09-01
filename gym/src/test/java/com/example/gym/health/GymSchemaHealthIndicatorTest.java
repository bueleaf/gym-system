package com.example.gym.health;

import com.example.gym.actuator.health.GymSchemaHealthIndicator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymSchemaHealthIndicatorTest {

    private static final String USER_COUNT_QUERY =
            "select count(u) from UserEntity u";
    private static final String TRAINING_COUNT_QUERY =
            "select count(t) from TrainingEntity t";

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Long> userCountQuery;

    @Mock
    private TypedQuery<Long> trainingCountQuery;

    private GymSchemaHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new GymSchemaHealthIndicator();
        indicator.setEntityManager(entityManager);
    }

    @Test
    void health_isUpWhenBothGymEntityTablesCanBeQueried() {
        when(entityManager.createQuery(USER_COUNT_QUERY, Long.class))
                .thenReturn(userCountQuery);
        when(entityManager.createQuery(TRAINING_COUNT_QUERY, Long.class))
                .thenReturn(trainingCountQuery);
        when(userCountQuery.getSingleResult()).thenReturn(4L);
        when(trainingCountQuery.getSingleResult()).thenReturn(9L);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("userTableUp", true)
                .containsEntry("trainingTableUp", true)
                .containsEntry("userCount", 4L)
                .containsEntry("trainingCount", 9L);
    }

    @Test
    void health_isDownWhenGymEntityTablesCannotBeQueried() {
        RuntimeException failure = new RuntimeException("database unavailable");
        when(entityManager.createQuery(USER_COUNT_QUERY, Long.class))
                .thenThrow(failure);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("reason", "Gym entities couldn't be queried")
                .containsEntry("error", failure.toString());
    }
}
