package com.example.gym.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainers")
@PrimaryKeyJoinColumn(name = "user_id")
public class TrainerEntity extends UserEntity
{
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingTypeEntity specialization;

    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    private Set<TraineeEntity> trainees = new HashSet<>();

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private Set<TrainingEntity> trainings = new HashSet<>();

    public TrainerEntity() {}

    public TrainingTypeEntity getSpecialization() { return specialization; }
    public void setSpecialization(TrainingTypeEntity specialization) {
        this.specialization = specialization;
    }

    public Set<TraineeEntity> getTrainees() { return trainees; }
    public void setTrainees(Set<TraineeEntity> trainees) { this.trainees = trainees; }

    public Set<TrainingEntity> getTrainings() { return trainings; }
    public void setTrainings(Set<TrainingEntity> trainings) { this.trainings = trainings; }
}
