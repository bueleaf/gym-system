package com.example.training.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "monthly_summaries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username",
                "summary_year",
                "summary_month"
        })
})
public class TrainerMonthlySummaryEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "isActive", nullable = false)
    private Boolean isActive;
    @Column(name = "summary_year", nullable = false)
    private Integer year;
    @Column(name = "summary_month", nullable = false)
    private Integer month;
    @Column(name = "training_duration_total", nullable = false)
    private Integer trainingDurationTotal;

    public TrainerMonthlySummaryEntity() {}

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getTrainingDurationTotal() {
        return trainingDurationTotal;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setTrainingDurationTotal(Integer trainingDurationTotal) {
        this.trainingDurationTotal = trainingDurationTotal;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
