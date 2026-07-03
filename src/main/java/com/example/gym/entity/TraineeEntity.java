package com.example.gym.entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainees")
@PrimaryKeyJoinColumn(name = "user_id")
public class TraineeEntity extends UserEntity
{
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private Set<TrainerEntity> trainers = new HashSet<>();

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<TrainingEntity> trainings = new HashSet<>();

    public void addTrainer(TrainerEntity trainer) {
        trainers.add(trainer);
        trainer.getTrainees().add(this);
    }

    public void removeTrainer(TrainerEntity trainer) {
        trainers.remove(trainer);
        trainer.getTrainees().remove(this);
    }

    public TraineeEntity() {}

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Set<TrainerEntity> getTrainers() { return trainers; }
    public void setTrainers(Set<TrainerEntity> trainers) { this.trainers = trainers; }

    public Set<TrainingEntity> getTrainings() { return trainings; }
    public void setTrainings(Set<TrainingEntity> trainings) { this.trainings = trainings; }
}
