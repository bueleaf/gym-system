package com.example.training.service;

import com.example.training.dao.TrainerMonthlySummaryDao;
import com.example.training.dto.request.TrainerWorkloadRequest;
import com.example.training.dto.response.TrainerMonthlyWorkloadResponse;
import com.example.training.entity.TrainerMonthlySummaryEntity;
import com.example.training.exception.InvalidWorkloadException;
import com.example.training.model.ActionType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {
    @Mock private TrainerMonthlySummaryDao trainerMonthlySummaryDao;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @Test
    void updateWorkloadCreatesSummaryForNewAddRequest() {
        TrainerWorkloadRequest request = request(ActionType.ADD, 60);
        when(trainerMonthlySummaryDao.findByUsernameAndMonthAndYear("trainer", 8, 2026))
                .thenReturn(Optional.empty());

        trainerWorkloadService.updateWorkload(request);

        ArgumentCaptor<TrainerMonthlySummaryEntity> summaryCaptor =
                ArgumentCaptor.forClass(TrainerMonthlySummaryEntity.class);
        verify(trainerMonthlySummaryDao).save(summaryCaptor.capture());
        assertThat(summaryCaptor.getValue())
                .usingRecursiveComparison()
                .comparingOnlyFields("username", "firstName", "lastName", "isActive", "year", "month", "trainingDurationTotal")
                .isEqualTo(summary("trainer", "Jane", "Doe", true, 2026, 8, 60));
    }

    @Test
    void updateWorkloadAddsDurationToExistingSummary() {
        TrainerMonthlySummaryEntity summary = summary("trainer", "Old", "Name", false, 2026, 8, 100);
        when(trainerMonthlySummaryDao.findByUsernameAndMonthAndYear("trainer", 8, 2026))
                .thenReturn(Optional.of(summary));

        trainerWorkloadService.updateWorkload(request(ActionType.ADD, 60));

        assertThat(summary.getTrainingDurationTotal()).isEqualTo(160);
        assertThat(summary.getFirstName()).isEqualTo("Jane");
        assertThat(summary.getLastName()).isEqualTo("Doe");
        assertThat(summary.getIsActive()).isTrue();
    }

    @Test
    void updateWorkloadSubtractsDurationFromExistingSummary() {
        TrainerMonthlySummaryEntity summary = summary("trainer", "Jane", "Doe", true, 2026, 8, 100);
        when(trainerMonthlySummaryDao.findByUsernameAndMonthAndYear("trainer", 8, 2026))
                .thenReturn(Optional.of(summary));

        trainerWorkloadService.updateWorkload(request(ActionType.DELETE, 60));

        assertThat(summary.getTrainingDurationTotal()).isEqualTo(40);
    }

    @Test
    void updateWorkloadRejectsDeletionGreaterThanExistingDuration() {
        TrainerMonthlySummaryEntity summary = summary("trainer", "Jane", "Doe", true, 2026, 8, 30);
        when(trainerMonthlySummaryDao.findByUsernameAndMonthAndYear("trainer", 8, 2026))
                .thenReturn(Optional.of(summary));

        assertThatThrownBy(() -> trainerWorkloadService.updateWorkload(request(ActionType.DELETE, 60)))
                .isInstanceOf(InvalidWorkloadException.class)
                .hasMessage("Cannot subtract 60 minutes from total workload of 30");
    }

    @Test
    void updateWorkloadRejectsDeletionWhenSummaryDoesNotExist() {
        when(trainerMonthlySummaryDao.findByUsernameAndMonthAndYear("trainer", 8, 2026))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerWorkloadService.updateWorkload(request(ActionType.DELETE, 60)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Object not found for removal");
    }

    @Test
    void getMonthlyWorkloadMapsStoredSummary() {
        TrainerMonthlySummaryEntity summary = summary("trainer", "Jane", "Doe", true, 2026, 8, 120);
        when(trainerMonthlySummaryDao.findByUsernameAndMonthAndYear("trainer", 8, 2026))
                .thenReturn(Optional.of(summary));

        TrainerMonthlyWorkloadResponse response =
                trainerWorkloadService.getMonthlyWorkload("trainer", 8, 2026);

        assertThat(response).isEqualTo(new TrainerMonthlyWorkloadResponse(
                "trainer", "Jane", "Doe", true, 2026, 8, 120));
    }

    @Test
    void getMonthlyWorkloadRejectsMissingSummary() {
        when(trainerMonthlySummaryDao.findByUsernameAndMonthAndYear("trainer", 8, 2026))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerWorkloadService.getMonthlyWorkload("trainer", 8, 2026))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No such training summary exists");
    }

    private TrainerWorkloadRequest request(ActionType actionType, int duration) {
        return new TrainerWorkloadRequest(
                "trainer", "Jane", "Doe", true, LocalDate.of(2026, 8, 1), duration, actionType);
    }

    private TrainerMonthlySummaryEntity summary(
            String username, String firstName, String lastName, boolean active,
            int year, int month, int duration) {
        TrainerMonthlySummaryEntity summary = new TrainerMonthlySummaryEntity();
        summary.setUsername(username);
        summary.setFirstName(firstName);
        summary.setLastName(lastName);
        summary.setIsActive(active);
        summary.setYear(year);
        summary.setMonth(month);
        summary.setTrainingDurationTotal(duration);
        return summary;
    }
}
