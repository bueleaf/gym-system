package com.example.training.service;

import com.example.training.document.MonthSummary;
import com.example.training.document.TrainerMonthlySummaryDocument;
import com.example.training.document.YearSummary;
import com.example.training.dto.request.TrainerWorkloadEvent;
import com.example.training.dto.response.TrainerMonthlyWorkloadResponse;
import com.example.training.exception.DocumentNotFoundException;
import com.example.training.exception.InvalidWorkloadException;
import com.example.training.model.ActionType;
import com.example.training.repository.TrainerMonthlySummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {
    @Mock private TrainerMonthlySummaryRepository trainerMonthlySummaryRepository;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @Test
    void updateWorkloadCreatesSummaryForNewAddRequest() {
        TrainerWorkloadEvent request = request(ActionType.ADD, 60);
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.empty());

        trainerWorkloadService.updateWorkload(request);

        ArgumentCaptor<TrainerMonthlySummaryDocument> summaryCaptor =
                ArgumentCaptor.forClass(TrainerMonthlySummaryDocument.class);
        verify(trainerMonthlySummaryRepository).save(summaryCaptor.capture());
        assertThat(summaryCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(summary("trainer", "Jane", "Doe", true, 2026, 8, 60));
    }

    @Test
    void updateWorkloadAddsDurationToExistingSummary() {
        TrainerMonthlySummaryDocument summary = summary("trainer", "Old", "Name", false, 2026, 8, 100);
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.of(summary));

        trainerWorkloadService.updateWorkload(request(ActionType.ADD, 60));

        assertThat(monthSummary(summary).getTrainingDurationTotal()).isEqualTo(160);
        assertThat(summary.getFirstName()).isEqualTo("Jane");
        assertThat(summary.getLastName()).isEqualTo("Doe");
        assertThat(summary.getIsActive()).isTrue();
        verify(trainerMonthlySummaryRepository).save(summary);
    }

    @Test
    void updateWorkloadSubtractsDurationFromExistingSummary() {
        TrainerMonthlySummaryDocument summary = summary("trainer", "Jane", "Doe", true, 2026, 8, 100);
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.of(summary));

        trainerWorkloadService.updateWorkload(request(ActionType.DELETE, 60));

        assertThat(monthSummary(summary).getTrainingDurationTotal()).isEqualTo(40);
        verify(trainerMonthlySummaryRepository).save(summary);
    }

    @Test
    void updateWorkloadAddsNewMonthWithoutChangingExistingMonth() {
        TrainerMonthlySummaryDocument summary =
                summary("trainer", "Jane", "Doe", true, 2026, 8, 100);
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.of(summary));

        trainerWorkloadService.updateWorkload(
                request(ActionType.ADD, LocalDate.of(2026, 9, 1), 60));

        assertThat(summary.getYears()).hasSize(1);
        assertThat(monthSummary(summary, 8).getTrainingDurationTotal()).isEqualTo(100);
        assertThat(monthSummary(summary, 9).getTrainingDurationTotal()).isEqualTo(60);
        verify(trainerMonthlySummaryRepository).save(summary);
    }

    @Test
    void updateWorkloadAddsNewYearWithoutChangingExistingYear() {
        TrainerMonthlySummaryDocument summary =
                summary("trainer", "Jane", "Doe", true, 2026, 8, 100);
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.of(summary));

        trainerWorkloadService.updateWorkload(
                request(ActionType.ADD, LocalDate.of(2027, 1, 1), 60));

        assertThat(summary.getYears()).hasSize(2);
        assertThat(monthSummary(summary, 2026, 8).getTrainingDurationTotal()).isEqualTo(100);
        assertThat(monthSummary(summary, 2027, 1).getTrainingDurationTotal()).isEqualTo(60);
        verify(trainerMonthlySummaryRepository).save(summary);
    }

    @Test
    void updateWorkloadRejectsDeletionForMissingMonthWithoutSaving() {
        TrainerMonthlySummaryDocument summary =
                summary("trainer", "Jane", "Doe", true, 2026, 8, 100);
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.of(summary));

        assertThatThrownBy(() -> trainerWorkloadService.updateWorkload(
                request(ActionType.DELETE, LocalDate.of(2026, 9, 1), 60)))
                .isInstanceOf(DocumentNotFoundException.class);

        assertThat(summary.getYears()).hasSize(1);
        assertThat(summary.getYears().getFirst().getMonths()).hasSize(1);
        verify(trainerMonthlySummaryRepository, never()).save(summary);
    }

    @Test
    void updateWorkloadRejectsDeletionGreaterThanExistingDuration() {
        TrainerMonthlySummaryDocument summary = summary("trainer", "Jane", "Doe", true, 2026, 8, 30);
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.of(summary));

        assertThatThrownBy(() -> trainerWorkloadService.updateWorkload(request(ActionType.DELETE, 60)))
                .isInstanceOf(InvalidWorkloadException.class)
                .hasMessage("Cannot subtract 60 minutes from total workload of 30");
    }

    @Test
    void updateWorkloadRejectsDeletionWhenSummaryDoesNotExist() {
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerWorkloadService.updateWorkload(request(ActionType.DELETE, 60)))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage("Document not found for removal");
    }

    @Test
    void getMonthlyWorkloadMapsStoredSummary() {
        TrainerMonthlySummaryDocument summary = summary("trainer", "Jane", "Doe", true, 2026, 8, 120);
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.of(summary));

        TrainerMonthlyWorkloadResponse response =
                trainerWorkloadService.getMonthlyWorkload("trainer", 8, 2026);

        assertThat(response).isEqualTo(new TrainerMonthlyWorkloadResponse(
                "trainer", "Jane", "Doe", true, 2026, 8, 120));
    }

    @Test
    void getMonthlyWorkloadRejectsMissingSummary() {
        when(trainerMonthlySummaryRepository.findByUsername("trainer"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerWorkloadService.getMonthlyWorkload("trainer", 8, 2026))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage("No such training summary exists");
    }

    private TrainerWorkloadEvent request(ActionType actionType, int duration) {
        return request(actionType, LocalDate.of(2026, 8, 1), duration);
    }

    private TrainerWorkloadEvent request(
            ActionType actionType, LocalDate date, int duration) {
        return new TrainerWorkloadEvent(
                "trainer", "Jane", "Doe", true, date, duration, actionType);
    }

    private TrainerMonthlySummaryDocument summary(
            String username, String firstName, String lastName, boolean active,
            int year, int month, int duration) {
        TrainerMonthlySummaryDocument summary = new TrainerMonthlySummaryDocument();
        summary.setUsername(username);
        summary.setFirstName(firstName);
        summary.setLastName(lastName);
        summary.setIsActive(active);
        MonthSummary monthSummary = new MonthSummary();
        monthSummary.setMonth(month);
        monthSummary.setTrainingDurationTotal(duration);

        YearSummary yearSummary = new YearSummary();
        yearSummary.setYear(year);
        yearSummary.setMonths(new ArrayList<>(List.of(monthSummary)));
        summary.setYears(new ArrayList<>(List.of(yearSummary)));
        return summary;
    }

    private MonthSummary monthSummary(TrainerMonthlySummaryDocument summary) {
        return summary.getYears().getFirst().getMonths().getFirst();
    }

    private MonthSummary monthSummary(TrainerMonthlySummaryDocument summary, int month) {
        return monthSummary(summary, 2026, month);
    }

    private MonthSummary monthSummary(
            TrainerMonthlySummaryDocument summary, int year, int month) {
        return summary.getYears().stream()
                .filter(yearSummary -> yearSummary.getYear().equals(year))
                .findFirst()
                .orElseThrow()
                .getMonths().stream()
                .filter(monthSummary -> monthSummary.getMonth().equals(month))
                .findFirst()
                .orElseThrow();
    }
}
