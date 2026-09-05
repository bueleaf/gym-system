package com.example.gym.health;

import com.example.gym.actuator.health.TrainingTypeHealthIndicator;
import com.example.gym.dao.TrainingTypeDao;
import com.example.gym.entity.TrainingTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeCatalogHealthIndicatorTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    private TrainingTypeHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new TrainingTypeHealthIndicator();
        indicator.setTrainingTypeDao(trainingTypeDao);
    }

    @Test
    void health_isUpWhenTrainingTypeCatalogContainsEntries() {
        when(trainingTypeDao.findAll())
                .thenReturn(List.of(mock(TrainingTypeEntity.class)));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("count", 1);
    }

    @Test
    void health_isDownWhenTrainingTypeCatalogIsEmpty() {
        when(trainingTypeDao.findAll()).thenReturn(List.of());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("reason", "No training types detected");
    }

    @Test
    void health_isDownWhenTrainingTypeCatalogCannotBeRead() {
        RuntimeException failure = new RuntimeException("database unavailable");
        when(trainingTypeDao.findAll()).thenThrow(failure);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("error", failure.toString());
    }
}
