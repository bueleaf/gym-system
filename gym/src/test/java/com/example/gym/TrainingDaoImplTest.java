package com.example.gym;

import com.example.gym.dao.TrainingDaoImpl;
import com.example.gym.entity.TrainingEntity;
import com.example.gym.dto.TraineeTrainingSearchCriteria;
import com.example.gym.dto.TrainerTrainingSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingDaoImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<TrainingEntity> query;

    private TrainingDaoImpl trainingDao;

    @BeforeEach
    void setUp() {
        trainingDao = new TrainingDaoImpl();
        ReflectionTestUtils.setField(
                trainingDao,
                "entityManager",
                entityManager
        );

        when(entityManager.createQuery(anyString(), eq(TrainingEntity.class)))
                .thenReturn(query);
        when(query.setParameter(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(query);
    }

    @Test
    void traineeSearch_appliesMatchingInclusiveDateRangeAndOptionalFilters() {
        TrainingEntity training = new TrainingEntity();
        when(query.getResultList()).thenReturn(List.of(training));

        List<TrainingEntity> result =
                trainingDao.findTraineeTrainingsByCriteria(
                        "john.doe",
                        new TraineeTrainingSearchCriteria(
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2026, 7, 31),
                                "  Mike  ",
                                "  Yoga  "
                        )
                );

        assertThat(result).containsExactly(training);

        String jpql = capturedJpql();
        assertThat(jpql).contains("t.trainingDate >= :fromDate");
        assertThat(jpql).contains("t.trainingDate <= :toDate");
        assertThat(jpql).contains("LOWER(CONCAT(");
        assertThat(jpql).contains("tr.firstName");
        assertThat(jpql).contains("tr.lastName");
        assertThat(jpql).contains("LOWER(tt.trainingTypeName) = :trainingTypeName");

        verify(query).setParameter(
                "fromDate",
                LocalDate.of(2026, 7, 1)
        );
        verify(query).setParameter(
                "toDate",
                LocalDate.of(2026, 7, 31)
        );
        verify(query).setParameter("counterpartName", "%mike%");
        verify(query).setParameter("trainingTypeName", "yoga");
    }

    @Test
    void traineeSearch_excludingDateRangeReturnsEmptyList() {
        when(query.getResultList()).thenReturn(List.of());

        List<TrainingEntity> result =
                trainingDao.findTraineeTrainingsByCriteria(
                        "john.doe",
                        new TraineeTrainingSearchCriteria(
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 8, 31),
                                null,
                                null
                        )
                );

        assertThat(result).isEmpty();
        verify(query).setParameter(
                "fromDate",
                LocalDate.of(2026, 8, 1)
        );
        verify(query).setParameter(
                "toDate",
                LocalDate.of(2026, 8, 31)
        );
    }

    @Test
    void traineeSearch_onlyFromDateAppliesLowerBound() {
        trainingDao.findTraineeTrainingsByCriteria(
                "john.doe",
                new TraineeTrainingSearchCriteria(
                        LocalDate.of(2026, 7, 1),
                        null,
                        null,
                        null
                )
        );

        String jpql = capturedJpql();
        assertThat(jpql).contains("t.trainingDate >= :fromDate");
        assertThat(jpql).doesNotContain("t.trainingDate <= :toDate");
        verify(query).setParameter(
                "fromDate",
                LocalDate.of(2026, 7, 1)
        );
        verify(query, never()).setParameter(eq("toDate"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void traineeSearch_onlyToDateAppliesUpperBound() {
        trainingDao.findTraineeTrainingsByCriteria(
                "john.doe",
                new TraineeTrainingSearchCriteria(
                        null,
                        LocalDate.of(2026, 7, 31),
                        null,
                        null
                )
        );

        String jpql = capturedJpql();
        assertThat(jpql).doesNotContain("t.trainingDate >= :fromDate");
        assertThat(jpql).contains("t.trainingDate <= :toDate");
        verify(query, never()).setParameter(eq("fromDate"), org.mockito.ArgumentMatchers.any());
        verify(query).setParameter(
                "toDate",
                LocalDate.of(2026, 7, 31)
        );
    }

    @Test
    void traineeSearch_nullFiltersAreIgnored() {
        trainingDao.findTraineeTrainingsByCriteria(
                "john.doe",
                new TraineeTrainingSearchCriteria(
                        null,
                        null,
                        null,
                        null
                )
        );

        String jpql = capturedJpql();
        assertThat(jpql).doesNotContain("fromDate");
        assertThat(jpql).doesNotContain("toDate");
        assertThat(jpql).doesNotContain("counterpartName");
        assertThat(jpql).doesNotContain("trainingTypeName");
    }

    @Test
    void trainerSearch_appliesDateRangeAndTraineeNameFilter() {
        when(query.getResultList()).thenReturn(List.of());

        List<TrainingEntity> result =
                trainingDao.findTrainerTrainingsByCriteria(
                        "mike.smith",
                        new TrainerTrainingSearchCriteria(
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2026, 7, 31),
                                "John"
                        )
                );

        assertThat(result).isEmpty();

        String jpql = capturedJpql();
        assertThat(jpql).contains("tr.username = :trainerUsername");
        assertThat(jpql).contains("t.trainingDate >= :fromDate");
        assertThat(jpql).contains("t.trainingDate <= :toDate");
        assertThat(jpql).contains("LOWER(CONCAT(");
        assertThat(jpql).contains("te.firstName");
        assertThat(jpql).contains("te.lastName");

        verify(query).setParameter("trainerUsername", "mike.smith");
        verify(query).setParameter(
                "fromDate",
                LocalDate.of(2026, 7, 1)
        );
        verify(query).setParameter(
                "toDate",
                LocalDate.of(2026, 7, 31)
        );
        verify(query).setParameter("counterpartName", "%john%");
    }

    @Test
    void trainerSearch_nonmatchingTraineeNameReturnsEmptyList() {
        when(query.getResultList()).thenReturn(List.of());

        List<TrainingEntity> result =
                trainingDao.findTrainerTrainingsByCriteria(
                        "mike.smith",
                        new TrainerTrainingSearchCriteria(
                                null,
                                null,
                                "Nobody"
                        )
                );

        assertThat(result).isEmpty();
        String jpql = capturedJpql();
        assertThat(jpql).contains("LOWER(CONCAT(");
        assertThat(jpql).contains("te.firstName");
        assertThat(jpql).contains("te.lastName");
        verify(query).setParameter("counterpartName", "%nobody%");
    }

    private String capturedJpql() {
        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(entityManager).createQuery(
                captor.capture(),
                eq(TrainingEntity.class)
        );

        return captor.getValue();
    }
}
