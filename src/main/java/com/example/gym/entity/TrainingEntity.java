package com.example.gym.entities;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trainings")
public class TrainingEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private TraineeEntity trainee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer;

    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingTypeEntity trainingType;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "training_duration", nullable = false)
    private Integer trainingDuration;

    public TrainingEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TraineeEntity getTrainee() { return trainee; }
    public void setTrainee(TraineeEntity trainee) { this.trainee = trainee; }

    public TrainerEntity getTrainer() { return trainer; }
    public void setTrainer(TrainerEntity trainer) { this.trainer = trainer; }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public TrainingTypeEntity getTrainingType() { return trainingType; }
    public void setTrainingType(TrainingTypeEntity trainingType)
    {
        this.trainingType = trainingType;
    }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public Integer getTrainingDuration() { return trainingDuration; }
    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }
}
